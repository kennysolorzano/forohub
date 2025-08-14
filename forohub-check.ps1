# forohub-check.ps1 (flexible)
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
[Console]::OutputEncoding = [Text.UTF8Encoding]::UTF8

# =======================
# CONFIG
# =======================
$baseUrl   = "http://localhost:8080"
$loginUrl  = "$baseUrl/login"
$meUrl     = "$baseUrl/auth/me"
$topicsUrl = "$baseUrl/topicos"

$adminUser = "admin"
$adminPass = "admin123"

$runStamp = (Get-Date).ToString("yyyyMMdd-HHmmss")

# =======================
# HELPERS
# =======================
function Write-Section { param([string]$Title) Write-Host "`n==== $Title ====" -ForegroundColor Cyan }
function Write-Status  { param([int]$Code)   Write-Host ("Status: {0}" -f $Code) }

function Invoke-Http {
  param(
    [Parameter(Mandatory=$true)][string]$Method,
    [Parameter(Mandatory=$true)][string]$Url,
    [hashtable]$Headers,
    [string]$ContentType,
    [byte[]]$BodyBytes
  )
  try {
    if ($Method -in @('GET','DELETE')) {
      $resp = Invoke-WebRequest -Method $Method -Uri $Url -Headers $Headers -ErrorAction Stop
    } else {
      if ($null -ne $BodyBytes) {
        $resp = Invoke-WebRequest -Method $Method -Uri $Url -Headers $Headers -ContentType $ContentType -Body $BodyBytes -ErrorAction Stop
      } else {
        $resp = Invoke-WebRequest -Method $Method -Uri $Url -Headers $Headers -ErrorAction Stop
      }
    }
    return @{ Status=[int]$resp.StatusCode; Body=$resp.Content; Headers=$resp.Headers }
  } catch {
    $we = $_.Exception
    $status = $null; $body = ""
    try { $status = [int]$we.Response.StatusCode.value__ } catch {}
    try {
      $sr = New-Object IO.StreamReader($we.Response.GetResponseStream())
      $body = $sr.ReadToEnd()
      $sr.Dispose()
    } catch {}
    return @{ Status=$status; Body=$body; Headers=@{} }
  }
}

function Utf8BytesFromObject {
  param($obj)
  $json = ($obj | ConvertTo-Json -Depth 10 -Compress)
  return [Text.Encoding]::UTF8.GetBytes($json)
}

function Test-HttpStatus {
  param(
    [Parameter(Mandatory=$true)][hashtable]$Resp,
    [Parameter(Mandatory=$true)][int[]]$AnyOf,
    [string]$FailMessage
  )
  if ($AnyOf -notcontains $Resp.Status) {
    Write-Status $Resp.Status
    if ($Resp.Body) { Write-Host $Resp.Body }
    if ($FailMessage) { Write-Host $FailMessage -ForegroundColor Red }
    exit 1
  }
}

# =======================
# 1) LOGIN
# =======================
Write-Section "1) LOGIN"
$loginPayload = @{ username = $adminUser; password = $adminPass }
$loginResp = Invoke-Http -Method POST -Url $loginUrl -ContentType "application/json; charset=utf-8" -BodyBytes (Utf8BytesFromObject $loginPayload)
Write-Status $loginResp.Status
Test-HttpStatus -Resp $loginResp -AnyOf 200,201 -FailMessage "Fallo login."

$token = ""
try { $token = ( $loginResp.Body | ConvertFrom-Json | Select-Object -ExpandProperty token ) } catch {}
if (-not $token) {
  Write-Host "No se pudo obtener token desde la respuesta del login." -ForegroundColor Red
  if ($loginResp.Body) { Write-Host $loginResp.Body }
  exit 1
}
Write-Host ("TOKEN: {0}" -f $token)
$authHeader = @{ Authorization = "Bearer $token" }

# =======================
# 2) /auth/me
# =======================
Write-Section "2) /auth/me"
$meResp = Invoke-Http -Method GET -Url $meUrl -Headers $authHeader
Write-Status $meResp.Status
Test-HttpStatus -Resp $meResp -AnyOf 200 -FailMessage "/auth/me no devolvió 200."

# =======================
# 3) GET /topicos (público)
# =======================
Write-Section "3) GET /topicos (público)"
$listResp = Invoke-Http -Method GET -Url "$($topicsUrl)?page=0&size=10"
Write-Status $listResp.Status
Test-HttpStatus -Resp $listResp -AnyOf 200 -FailMessage "GET /topicos público no devolvió 200."

# =======================
# 4) POST /topicos (válido, UTF-8)
# =======================
Write-Section "4) POST /topicos (válido, UTF-8)"
$titleBase = "Caso OK $runStamp"
$createObj = @{
  title   = $titleBase
  message = "Contenido con tildes y signos: ¿áéíóú? ñ"
  author  = "Kenny"
  course  = "Spring Boot"
}
$createResp = Invoke-Http -Method POST -Url $topicsUrl -Headers $authHeader -ContentType "application/json; charset=utf-8" -BodyBytes (Utf8BytesFromObject $createObj)
Write-Status $createResp.Status
if ($createResp.Status -eq 200) {
  Write-Host "⚠️ El endpoint devolvió 200 en vez de 201. Aceptamos, pero sería recomendable 201 Created." -ForegroundColor Yellow
} else {
  Test-HttpStatus -Resp $createResp -AnyOf 201 -FailMessage "POST /topicos válido debería devolver 201."
}

$created = $null
try { $created = $createResp.Body | ConvertFrom-Json } catch {}
if (-not $created) {
  Write-Host "No se pudo parsear el body del POST válido." -ForegroundColor Red
  Write-Host $createResp.Body
  exit 1
}
$createdId = $created.id
if (-not $createdId) {
  Write-Host "El body del POST no trae 'id'." -ForegroundColor Red
  Write-Host $createResp.Body
  exit 1
}

if ($createResp.Status -eq 201) {
  $loc = $createResp.Headers["Location"]
  if (-not $loc) {
    Write-Host "⚠️ Falta header Location en 201 Created." -ForegroundColor Yellow
  } else {
    $expectedLoc = "/topicos/$createdId"
    if ($loc -ne $expectedLoc) {
      Write-Host "⚠️ Location es '$loc' pero se esperaba '$expectedLoc'." -ForegroundColor Yellow
    }
  }
}

# =======================
# 5) POST /topicos sin token (401/403)
# =======================
Write-Section "5) POST /topicos sin token (esperado 401/403)"
$noTokenObj = @{
  title   = "$titleBase - NO TOKEN"
  message = "Prueba sin token"
  author  = "Kenny"
  course  = "Spring Boot"
}
$noTokenResp = Invoke-Http -Method POST -Url $topicsUrl -ContentType "application/json; charset=utf-8" -BodyBytes (Utf8BytesFromObject $noTokenObj)
Write-Status $noTokenResp.Status
if (@(401,403) -notcontains $noTokenResp.Status) {
  Write-Host "Se esperaba 401 o 403 al crear sin token." -ForegroundColor Red
  if ($noTokenResp.Body) { Write-Host $noTokenResp.Body }
  exit 1
}

# =======================
# 6) POST /topicos inválido (400)
# =======================
Write-Section "6) POST /topicos inválido (400 por Bean Validation)"
$badObj = @{ message = "sin título"; author = "Kenny"; course = "SB" }
$badResp = Invoke-Http -Method POST -Url $topicsUrl -Headers $authHeader -ContentType "application/json; charset=utf-8" -BodyBytes (Utf8BytesFromObject $badObj)
Write-Status $badResp.Status
Test-HttpStatus -Resp $badResp -AnyOf 400 -FailMessage "Se esperaba 400 por validación."
if ($badResp.Body) { Write-Host $badResp.Body }

# =======================
# 7) POST duplicado (409)
# =======================
Write-Section "7) POST duplicado (409)"
$dupObj = @{
  title   = $titleBase
  message = "texto"
  author  = "Kenny"
  course  = "Spring Boot"
}
$dupResp = Invoke-Http -Method POST -Url $topicsUrl -Headers $authHeader -ContentType "application/json; charset=utf-8" -BodyBytes (Utf8BytesFromObject $dupObj)
Write-Status $dupResp.Status
Test-HttpStatus -Resp $dupResp -AnyOf 409 -FailMessage "Se esperaba 409 por duplicidad (índice único)."
if ($dupResp.Body) { Write-Host $dupResp.Body }

# =======================
# 8) GET /topicos/{id}
# =======================
Write-Section "8) GET /topicos/{id}"
$getOneResp = Invoke-Http -Method GET -Url "$($topicsUrl)/$createdId"
Write-Status $getOneResp.Status
Test-HttpStatus -Resp $getOneResp -AnyOf 200 -FailMessage "GET /topicos/{id} no devolvió 200."

# =======================
# 9) PUT /topicos/{id}
# =======================
Write-Section "9) PUT /topicos/{id}"
$updObj = @{ title = "$titleBase - EDITADO"; message = "Mensaje editado"; course = "Spring" }
$updResp = Invoke-Http -Method PUT -Url "$($topicsUrl)/$createdId" -Headers $authHeader -ContentType "application/json; charset=utf-8" -BodyBytes (Utf8BytesFromObject $updObj)
Write-Status $updResp.Status
Test-HttpStatus -Resp $updResp -AnyOf 200 -FailMessage "PUT /topicos/{id} no devolvió 200."

# =======================
# 10) DELETE /topicos/{id}
# =======================
Write-Section "10) DELETE /topicos/{id}"
$delResp = Invoke-Http -Method DELETE -Url "$($topicsUrl)/$createdId" -Headers $authHeader
Write-Status $delResp.Status
if (@(200,204) -notcontains $delResp.Status) {
  Write-Host "Se esperaba 204 (o 200) al eliminar." -ForegroundColor Red
  if ($delResp.Body) { Write-Host $delResp.Body }
  exit 1
}

# =======================
# 11) Paginación
# =======================
Write-Section "11) Paginación"
$pg0 = Invoke-Http -Method GET -Url "$($topicsUrl)?page=0&size=2"
Write-Status $pg0.Status
Test-HttpStatus -Resp $pg0 -AnyOf 200 -FailMessage "GET page=0&size=2 no devolvió 200."
$pg1 = Invoke-Http -Method GET -Url "$($topicsUrl)?page=1&size=2"
Write-Status $pg1.Status
Test-HttpStatus -Resp $pg1 -AnyOf 200 -FailMessage "GET page=1&size=2 no devolvió 200."

Write-Host "`nChecklist finalizado." -ForegroundColor Green
exit 0
