$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$out = Join-Path $root "out"
Set-Location $root
& java -cp $out com.hospital.appointment.HospitalAppointmentApp @args
