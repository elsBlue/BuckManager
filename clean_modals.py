import re
import os

path = r'app/src/main/java/com/buckmanager/app/ui/components/Modals.kt'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

# 1. Remove exportJsonLauncher (lines ~665-671)
text = re.sub(r'    val exportJsonLauncher = rememberLauncherForActivityResult\(\n        contract = ActivityResultContracts\.CreateDocument\("application/json"\)\n    \) { uri ->\n        if \(uri != null\) {\n            onExportJson\(uri\)\n        }\n    }\n', '', text)

# 2. Remove states: showShareCustomizationDialog, customizationJsonText, customizationInputText
text = re.sub(r'    var showShareCustomizationDialog by remember \{ mutableStateOf\(false\) \}\n', '', text)
text = re.sub(r'    var customizationJsonText by remember \{ mutableStateOf\(""\) \}\n', '', text)
text = re.sub(r'    var customizationInputText by remember \{ mutableStateOf\(""\) \}\n', '', text)

# 3. Change padding
text = re.sub(r'\.padding\(horizontal = 24\.dp, vertical = 24\.dp\)', r'.padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 24.dp)', text)

# 4. Extract Sign Out Row and insert it after Profile Box
# The Profile Box ends with:
#                             Text(
#                                 text = userEmail ?: "Local Offline Session",
#                                 color = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF5A667A),
#                                 fontSize = 12.sp
#                             )
#                         }
#                     }
sign_out_block = """                    // Sign Out / In Row
                    Surface(
                        onClick = {
                            if (userEmail != null) onLogoutClick() else onLoginClick()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        color = Color.Transparent,
                        border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF282436) else Color(0xFFE2E8F0))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (userEmail != null) Icons.Default.ExitToApp else Icons.Default.Login,
                                contentDescription = "Sign Out",
                                tint = if (userEmail != null) Color(0xFFFB7185) else GoldAccent,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (userEmail != null) "Sign Out" else "Sign In with Google",
                                color = if (userEmail != null) Color(0xFFFB7185) else GoldAccent,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }"""

profile_box_end = r"""                            Text(
                                text = userEmail \?\: "Local Offline Session",
                                color = if \(isDarkMode\) Color\(0xFF9CA3AF\) else Color\(0xFF5A667A\),
                                fontSize = 12\.sp
                            \)
                        \}
                    \}"""

# First, remove the old Sign Out row (it might have slightly different structure)
old_sign_out_pattern = r'                            // Sign Out / In Row\n                            Surface\(\n                                onClick = \{\n                                    if \(userEmail != null\) onLogoutClick\(\) else onLoginClick\(\)\n                                \},\n                                color = Color\.Transparent\n                            \) \{\n                                Row\(\n                                    modifier = Modifier\n                                        \.fillMaxWidth\(\)\n                                        \.padding\(16\.dp\),\n                                    verticalAlignment = Alignment\.CenterVertically\n                                \) \{\n                                    Icon\(\n                                        imageVector = if \(userEmail != null\) Icons\.Default\.ExitToApp else Icons\.Default\.Login,\n                                        contentDescription = "Sign Out",\n                                        tint = if \(userEmail != null\) Color\(0xFFFB7185\) else GoldAccent,\n                                        modifier = Modifier\.size\(24\.dp\)\n                                    \)\n                                    Spacer\(modifier = Modifier\.width\(16\.dp\)\)\n                                    Text\(\n                                        text = if \(userEmail != null\) "Sign Out" else "Sign In with Google",\n                                        color = if \(userEmail != null\) Color\(0xFFFB7185\) else GoldAccent,\n                                        fontSize = 14\.sp,\n                                        fontWeight = FontWeight\.Bold\n                                    \)\n                                \}\n                            \}'

text = re.sub(old_sign_out_pattern, '', text)

# Also remove the divider before it
text = re.sub(r'                            Divider\(color = if \(isDarkMode\) Color\(0xFF282436\) else Color\(0xFFE2E8F0\)\)\n\n\s*\}', '}', text) # Just cleanup any trailing divider
# Wait, actually I will just use re.sub on the specific sections.

# 5. Remove Local Snapshot (from // Local Snapshot Card to just before // Export JSON Card)
text = re.sub(r'\s*// Local Snapshot Card[\s\S]*?(?=\s*// Export JSON Card)', '', text)

# 6. Remove Export JSON (from // Export JSON Card to just before // 4. SETTINGS)
text = re.sub(r'\s*// Export JSON Card[\s\S]*?(?=\s*// 4\. SETTINGS)', '\n\n                    ', text)

# 7. Remove Daily Reminder and Share / Import Theme Code 
# Starting from the divider after Currency Row until the end of the Surface Column
currency_row_end_pattern = r'                                \}\n                            \}\n                            Divider\(color = if \(isDarkMode\) Color\(0xFF282436\) else Color\(0xFFE2E8F0\)\)\n                            Row\([\s\S]*?// Sign Out / In Row'

# Actually, it's easier to manually replace the bottom half of the settings Column.
settings_bottom_start = r'                            // Currency Row\n                            Surface\(\n                                onClick = \{ showCurrencyModal = true \},\n                                color = Color\.Transparent\n                            \) \{\n                                Row\(\n                                    modifier = Modifier\n                                        \.fillMaxWidth\(\)\n                                        \.padding\(16\.dp\),\n                                    horizontalArrangement = Arrangement\.SpaceBetween,\n                                    verticalAlignment = Alignment\.CenterVertically\n                                \) \{\n                                    Row\(verticalAlignment = Alignment\.CenterVertically\) \{\n                                        Icon\(\n                                            imageVector = Icons\.Default\.MonetizationOn,\n                                            contentDescription = "Currency",\n                                            tint = Color\(0xFF34D399\),\n                                            modifier = Modifier\.size\(24\.dp\)\n                                        \)\n                                        Spacer\(modifier = Modifier\.width\(16\.dp\)\)\n                                        Column \{\n                                            Text\("Currency Settings", color = if \(isDarkMode\) Color\.White else Color\(0xFF121926\), fontSize = 14\.sp, fontWeight = FontWeight\.SemiBold\)\n                                            Text\(com\.example\.buckmanager\.model\.CurrencyConfig\.currencyCode, color = if \(isDarkMode\) Color\(0xFF9CA3AF\) else Color\(0xFF5A667A\), fontSize = 11\.sp\)\n                                        \}\n                                    \}\n                                    Icon\(\n                                        imageVector = Icons\.Default\.ChevronRight,\n                                        contentDescription = null,\n                                        tint = if \(isDarkMode\) Color\(0xFF9CA3AF\) else Color\(0xFF5A667A\)\n                                    \)\n                                \}\n                            \}\n'

# Find everything from currency_row_end to the end of the Settings Column, and replace it with just the Currency Row
settings_column_end = r'                        \}\n                    \}\n\n                    if \(statusMessage != null\)'

pattern_to_replace = settings_bottom_start + r'[\s\S]*?' + settings_column_end
replacement = settings_bottom_start + r'                        }\n                    }\n\n                    if (statusMessage != null)'
text = re.sub(pattern_to_replace, replacement, text)

# 8. Remove the Share / Import Customization Dialog
dialog_pattern = r'\s*// SHARE / IMPORT CUSTOMIZATION DIALOG[\s\S]*?(?=\s*CurrencyModal)'
text = re.sub(dialog_pattern, '\n    ', text)

# 9. Insert Sign Out below Profile Box
# Use a lambda to avoid backslash escaping issues in the replacement string
text = re.sub(profile_box_end, lambda m: m.group(0) + '\n\n' + sign_out_block, text)

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)

print("Done")
