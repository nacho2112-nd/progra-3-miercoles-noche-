param([string]$docx, [string]$pdf)
# Abre el .docx en Word, actualiza el índice y los campos, lo guarda y exporta el PDF.
$word = New-Object -ComObject Word.Application
$word.Visible = $false
$word.DisplayAlerts = 0
try {
    $d = $word.Documents.Open($docx, $false, $false)
    foreach ($toc in $d.TablesOfContents) { $toc.Update() }
    $d.Fields.Update() | Out-Null
    foreach ($toc in $d.TablesOfContents) { $toc.Update() }
    $d.Save()
    $d.ExportAsFixedFormat($pdf, 17)   # 17 = wdExportFormatPDF
    try { $d.Close($false) } catch { }
    Write-Output "ok $pdf"
} finally {
    $word.Quit()
    [System.Runtime.InteropServices.Marshal]::ReleaseComObject($word) | Out-Null
}
