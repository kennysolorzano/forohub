# forohub-check-stricto.ps1 (estricto)
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
[Console]::OutputEncoding = [Text.UTF8Encoding]::UTF8

$baseUrl   = "http://localhost:8080"
$loginUrl  = "$baseUrl/login"
$meUrl     = "$baseUrl/auth/me"
$topicsUrl = "$baseUrl/topicos"

$adminUser = "admin"
$adminPass = "admin123"
$runStamp = (Get-Date).ToString("yyyyMMdd-HHmmss")

function W { param([string]$t) Write-Host "`n==== $t ====" -ForegroundColor Cyan }
function S { param([int]$c)   Write-Host ("Status: {0}" -f $c) }
function Http {
  param([string]$Method,[string]$Url,[hashtable]$Headers,[string]$ContentType,[byte[]]$BodyBytes)
  try {
    if ($Method -in @('GET','DELETE')) {
      $r = Invoke-WebRequest -Method $Method -Uri $Url -Headers $Headers -ErrorAction Stop
    } else {
      if ($null -ne $BodyBytes) {
        $r = Invoke-WebRequest -Method $Method -Uri $Url -Headers $Headers -ContentType $ContentType -Body $BodyBytes -ErrorAction Stop
      } else {
        $r = Invoke-WebRequest -Method $Method -Uri $Url -Headers $Headers -ErrorAction Stop
      }
    }
    return @{ Status=[int]$r.StatusCode; Body=$r.Content; Headers=$r.Headers }
  } catch {
    $we = $_.Exception; $st=$null; $bd=""
    try { $st = [int]$we.Response.StatusCode.value__ } catch {}
    try { $sr=New-Object IO.StreamReader($we.Response.GetResponseStream()); $bd=$sr.ReadToEnd(); $sr.Dispose() } catch {}
    return @{ Status=$st; Body=$bd; Headers=@{} }
  }
}
function J { param($o) [Text.Encoding]::UTF8.GetBytes(($o|ConvertTo-Json -Depth 10 -Compress)) }
function Test-HttpStatus {
  param([hashtable]$R,[int[]]$AnyOf,[string]$Msg)
  if ($AnyOf -notcontains $R.Status) {
    S $R.Status; if ($R.Body){Write-Host $R.Body}; if($Msg){Write-Host $Msg -ForegroundColor Red}; exit 1
  }
}

# 1) Login
W "1) LOGIN"
$loginR = Http POST $loginUrl $null "application/json; charset=utf-8" (J @{username=$adminUser;password=$adminPass})
S $loginR.Status
Test-HttpStatus $loginR @(200,201) "Fallo login."
$token=""; try{$token=($loginR.Body|ConvertFrom-Json).token}catch{}
if(-not $token){Write-Host "No token en login." -ForegroundColor Red; if($loginR.Body){Write-Host $loginR.Body}; exit 1}
$H=@{Authorization="Bearer $token"}; Write-Host ("TOKEN: {0}" -f $token)

# 2) /auth/me
W "2) /auth/me"
$meR = Http GET $meUrl $H
S $meR.Status
Test-HttpStatus $meR @(200) "/auth/me no devolvió 200."

# 3) GET listado público
W "3) GET /topicos (público)"
$lst = Http GET "$($topicsUrl)?page=0&size=10" $null
S $lst.Status
Test-HttpStatus $lst @(200) "GET /topicos público no devolvió 200."

# 4) POST válido -> EXIGE 201 + Location correcto
W "4) POST /topicos (válido, UTF-8) -> exige 201 + Location"
$titleBase="Caso OK $runStamp"
$create=Http POST $topicsUrl $H "application/json; charset=utf-8" (J @{
  title=$titleBase; message="Contenido con tildes y signos: ¿áéíóú? ñ"; author="Kenny"; course="Spring Boot"
})
S $create.Status
Test-HttpStatus $create @(201) "Se esperaba 201 Created estrictamente."
$created=$null; try{$created=$create.Body|ConvertFrom-Json}catch{}
if(-not $created){Write-Host "No se pudo parsear JSON de creación." -ForegroundColor Red; Write-Host $create.Body; exit 1}
$createdId=$created.id
if(-not $createdId){Write-Host "Creación sin 'id'." -ForegroundColor Red; exit 1}
$loc=$create.Headers["Location"]
$expected="/topicos/$createdId"
if(-not $loc -or $loc -ne $expected){
  Write-Host "Header Location inválido. Recibido='$loc' esperado='$expected'." -ForegroundColor Red; exit 1
}

# 5) POST sin token -> 401/403
W "5) POST /topicos sin token (401/403)"
$noTok = Http POST $topicsUrl $null "application/json; charset=utf-8" (J @{
  title="$titleBase - NO TOKEN"; message="Prueba sin token"; author="Kenny"; course="Spring Boot"
})
S $noTok.Status
Test-HttpStatus $noTok @(401,403) "Se esperaba 401 o 403 sin token."

# 6) POST inválido -> 400
W "6) POST /topicos inválido (400)"
$bad = Http POST $topicsUrl $H "application/json; charset=utf-8" (J @{message="sin título"; author="Kenny"; course="SB"})
S $bad.Status
Test-HttpStatus $bad @(400) "Se esperaba 400 por validación."

# 7) POST duplicado -> 409
W "7) POST duplicado (409)"
$dup = Http POST $topicsUrl $H "application/json; charset=utf-8" (J @{title=$titleBase; message="texto"; author="Kenny"; course="Spring Boot"})
S $dup.Status
Test-HttpStatus $dup @(409) "Se esperaba 409 por duplicidad."

# 8) GET /topicos/{id}
W "8) GET /topicos/{id}"
$one = Http GET "$($topicsUrl)/$createdId" $null
S $one.Status
Test-HttpStatus $one @(200) "GET /topicos/{id} no devolvió 200."

# 9) PUT /topicos/{id}
W "9) PUT /topicos/{id}"
$upd = Http PUT "$($topicsUrl)/$createdId" $H "application/json; charset=utf-8" (J @{title="$titleBase - EDITADO"; message="Mensaje editado"; course="Spring"})
S $upd.Status
Test-HttpStatus $upd @(200) "PUT /topicos/{id} no devolvió 200."

# 10) DELETE /topicos/{id} -> exige 204 (estricto)
W "10) DELETE /topicos/{id} (exige 204)"
$del = Http DELETE "$($topicsUrl)/$createdId" $H
S $del.Status
Test-HttpStatus $del @(204) "Se esperaba 204 No Content al eliminar."

# 11) Paginación
W "11) Paginación"
$p0 = Http GET "$($topicsUrl)?page=0&size=2" $null
S $p0.Status
Test-HttpStatus $p0 @(200) "GET page=0&size=2 no devolvió 200."
$p1 = Http GET "$($topicsUrl)?page=1&size=2" $null
S $p1.Status
Test-HttpStatus $p1 @(200) "GET page=1&size=2 no devolvió 200."

Write-Host "`nChecklist ESTRICTO finalizado." -ForegroundColor Green
exit 0
