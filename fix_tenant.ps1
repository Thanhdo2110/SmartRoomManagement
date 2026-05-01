$filePath = 'c:\Users\ad\AndroidStudioProjects\SmartRoomManagement2\app\src\main\java\com\example\smartroommanagement\ui\activity\RoomDetailActivity.java'
$content = [System.IO.File]::ReadAllText($filePath)

$oldText = "existingTenant.setName(name);"
$idx = $content.IndexOf($oldText)

if ($idx -ge 0) {
    # Find the start of the block: "} else {" before this line
    $blockStart = $content.LastIndexOf("} else {", $idx)
    # Find the end: "viewModel.updateTenant(existingTenant);" after this
    $updateLine = "viewModel.updateTenant(existingTenant);"
    $updateIdx = $content.IndexOf($updateLine, $idx)
    $blockEnd = $updateIdx + $updateLine.Length
    
    $oldBlock = $content.Substring($blockStart, $blockEnd - $blockStart)
    Write-Host "FOUND block at $blockStart to $blockEnd"
    Write-Host "OLD:"
    Write-Host $oldBlock
    
    # Detect line ending used
    $lineEnding = "`r`n"
    
    $newBlock = "} else {" + $lineEnding + `
        "                    // Copy constructor de DiffUtil nhan dien thay doi" + $lineEnding + `
        "                    TenantEntity updatedTenant = new TenantEntity(existingTenant);" + $lineEnding + `
        "                    updatedTenant.setName(name);" + $lineEnding + `
        "                    updatedTenant.setPhone(phone);" + $lineEnding + `
        "                    updatedTenant.setIdentityCard(identity);" + $lineEnding + `
        "                    updatedTenant.setDeposit(deposit);" + $lineEnding + `
        "                    updatedTenant.setStartDate(startDate);" + $lineEnding + `
        "                    updatedTenant.setHometown(hometown);" + $lineEnding + `
        "                    updatedTenant.setBirthDate(birthdate);" + $lineEnding + `
        "                    updatedTenant.setContractTerm(contractTerm);" + $lineEnding + `
        "                    viewModel.updateTenant(updatedTenant);"
    
    $content = $content.Substring(0, $blockStart) + $newBlock + $content.Substring($blockEnd)
    [System.IO.File]::WriteAllText($filePath, $content)
    Write-Host ""
    Write-Host "SUCCESS: RoomDetailActivity.java updated!"
} else {
    Write-Host "FAILED: Could not find target content"
}
