;
; $Id$
;
; Installer include for use with all flavors of the installer

  ; The directory from which we will find our files at compile time
  !define RSRCDIR "${LOCALE}"
  !define DATADIR "..\..\getdown"

  !define MUI_FILE "savefile"
  !define INSTALLER_VERSION "1.0"

  CRCCheck On
  OutFile ${OUTFILENAME}

  RequestExecutionLevel user

;--------------------------------
;General

  !define MUI_ICON "${RSRCDIR}\install_icon.ico"
  !define MUI_UNICON "${RSRCDIR}\uninstall_icon.ico"
  !define MUI_WELCOMEFINISHPAGE_BITMAP "${RSRCDIR}\branding.bmp"
  !define MUI_UNWELCOMEFINISHPAGE_BITMAP "${RSRCDIR}\branding.bmp"

  !include "MUI.nsh"


;--------------------------------
;Modern UI Configuration

  !insertmacro MUI_PAGE_WELCOME
  !insertmacro MUI_PAGE_DIRECTORY
  !insertmacro MUI_PAGE_INSTFILES
  !insertmacro MUI_PAGE_FINISH

  !insertmacro MUI_UNPAGE_WELCOME
  !insertmacro MUI_UNPAGE_CONFIRM
  !insertmacro MUI_UNPAGE_INSTFILES


;--------------------------------
;Language jockeying

  ; Load up our localized messages
  !include "${RSRCDIR}\messages.nlf"

  Name "${NAME}"
  InstallDir "$PROGRAMFILES\${INSTALL_DIR}"

  !define MUI_BRANDINGTEXT "$(branding)"
  !define MUI_DIRECTORYPAGE_TEXT_TOP "$(install_where)"


;--------------------------------
;Data

  !define REQUIRED_JDKVERSION "1.6"
  !define JDKVERSION "1.6"

  AutoCloseWindow true  ; close the window when the install is done
  ShowInstDetails nevershow  ;hide  ;show
  ShowUninstDetails show
  XPStyle on


;-------------------------------------------------------------
Function .onInit

  ; install things only for the current user
  SetShellVarContext current

  ; Check to see if they already have the app installed
  ; Note: Prior to 04/04/07 we used to store our registry keys in HKLM, same location otherwise.
  ReadRegStr $R0 HKCU "SOFTWARE\${NAME}" INSTALL_DIR_REG_KEY
  StrCmp $R0 "" SetInstdir
  ClearErrors
  IfFileExists "$R0\$(shortcut_name).lnk" 0 SetInstdir
  !ifndef AUTORUN_INSTALLED
    Push $CMDLINE
    Push "/run"
    Call StrStr
    Pop $R1
    StrCmp $R1 "" 0 RunAlreadyInstalled
    MessageBox MB_YESNOCANCEL|MB_ICONQUESTION "$(already_installed)" \
        IDNO AskReinstall IDCANCEL Done
  !endif
  RunAlreadyInstalled:
  ExecShell "" "$R0\$(shortcut_name).lnk"
  Done:
  Quit

  AskReinstall:
  MessageBox MB_YESNO|MB_ICONQUESTION "$(reinstall)" \
    IDNO Done

  SetInstdir:
  StrCpy $INSTDIR "$APPDATA\${INSTALL_DIR}"

  ClearErrors

FunctionEnd


;-------------------------------------------------------------
Section "Install" InstStuff
  Var /GLOBAL JdkVers ; only global variables are supported, yay
  Var /GLOBAL JdkHome

  ; create our installation directory
  ClearErrors
  CreateDirectory "$INSTDIR"
  IfErrors 0 CheckForJDK
     MessageBox MB_OK|MB_ICONSTOP "$(no_create_instdir)"
     Quit

  ; check for the necessary JDK
  CheckForJDK:
  ClearErrors
  ReadRegStr $JdkVers HKLM "SOFTWARE\JavaSoft\Java Development Kit" "CurrentVersion"
  IfErrors NeedsRequiredJDK
  Push ${REQUIRED_JDKVERSION}
  Push $JdkVers
  Call CompareVersions
  Pop $R0
  IntCmp $R0 1 LocateJDK NeedsRequiredJDK

  LocateJDK:
  ReadRegStr $JdkHome HKLM "SOFTWARE\JavaSoft\Java Development Kit\$JdkVers" "JavaHome"
  StrCmp $JdkHome "" NeedsRequiredJDK
  IfFileExists "$JdkHome\bin\javaw.exe" 0 NeedsRequiredJDK
    Goto HasRequiredJDK

  NeedsRequiredJDK:
; TODO: tell them to install Java

  HasRequiredJDK:
  ClearErrors

  ; Install Getdown and the configuration
  SetOutPath $INSTDIR
  File "${DATADIR}\getdown.jar"
  File "jRegistryKey.dll"
  File "${DATADIR}\background.png"
  File "${RSRCDIR}\app_icon.ico"

  ; Create our bootstrap getdown.txt file
  FileOpen $9 "$INSTDIR\getdown.txt" "w"
  FileWrite $9 "appbase = ${APPBASE}\r$\n"
  FileClose $9

  ; Create our main launcher "shortcut"
  CreateShortCut "$INSTDIR\$(shortcut_name).lnk" \
                 "$JdkHome\bin\javaw.exe" "-jar getdown.jar ." \
                 "$INSTDIR\app_icon.ico" "" "" "" "$(shortcut_hint)"

  ; Write the uninstaller
  WriteUninstaller "$INSTDIR\$(uninstaller_name)"

  ; Create shortcuts in the start menu and on the desktop
  CreateShortCut "$SMPROGRAMS\${NAME}.lnk" \
                 "$INSTDIR\$(shortcut_name).lnk"
  CreateShortCut "$DESKTOP\${NAME}.lnk" \
                 "$INSTDIR\$(shortcut_name).lnk"

  ; Set up registry stuff
  WriteRegStr HKCU "SOFTWARE\${NAME}" INSTALL_DIR_REG_KEY $INSTDIR
  WriteRegStr HKCU "SOFTWARE\${NAME}" PRODUCT_VERSION_REG_KEY ${INSTALLER_VERSION}

  StrCpy $R0 "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\${NAME}"
  WriteRegStr HKCU $R0 "DisplayName" "${NAME}"
  WriteRegStr HKCU $R0 "UninstallString" "$INSTDIR\$(uninstaller_name)"
  WriteRegDWORD HKCU $R0 "NoModify" 1
SectionEnd


;-------------------------------------------------------------
; Set up the uninstall
Section "Uninstall"
  SetShellVarContext current

  RMDir /r "$INSTDIR"
  RMDir /r "$SMPROGRAMS\${NAME}"
  Delete "$DESKTOP\${NAME}.lnk"
  DeleteRegKey HKCU "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\${NAME}"
  DeleteRegKey HKCU "SOFTWARE\${NAME}"
SectionEnd


Function .onInstSuccess
  ; Now run Getdown which will download everything else and finish the job
  IfFileExists "$INSTDIR\$(shortcut_name).lnk" 0 itfailed
    ExecShell "" "$INSTDIR\$(shortcut_name).lnk"
  itfailed:
FunctionEnd


;-------------------------------------------------------------------------------
 ; CompareVersions
 ; input:
 ;    top of stack = existing version
 ;    top of stack-1 = needed version
 ; output:
 ;    top of stack = 1 if current version => neded version, else 0
 ; version is a string in format "xx.xx.xx.xx" (number of interger sections
 ; can be different in needed and existing versions)

Function CompareVersions
   ; stack: existing ver | needed ver
   Exch $R0
   Exch
   Exch $R1
   ; stack: $R1|$R0

   Push $R1
   Push $R0
   ; stack: e|n|$R1|$R0

   ClearErrors
   loop:
      IfErrors VersionNotFound
      Strcmp $R0 "" VersionTestEnd

      Call ParseVersion
      Pop $R0
      Exch

      Call ParseVersion
      Pop $R1
      Exch

      IntCmp $R1 $R0 +1 VersionOk VersionNotFound
      Pop $R0
      Push $R0

   goto loop

   VersionTestEnd:
      Pop $R0
      Pop $R1
      Push $R1
      Push $R0
      StrCmp $R0 $R1 VersionOk VersionNotFound

   VersionNotFound:
      StrCpy $R0 "0"
      Goto end

   VersionOk:
      StrCpy $R0 "1"
end:
   ; stack: e|n|$R1|$R0
   Exch $R0
   Pop $R0
   Exch $R0
   ; stack: res|$R1|$R0
   Exch
   ; stack: $R1|res|$R0
   Pop $R1
   ; stack: res|$R0
   Exch
   Pop $R0
   ; stack: res
FunctionEnd

;-----------------------------------------------------------------------------
 ; ParseVersion
 ; input:
 ;      top of stack = version string ("xx.xx.xx.xx")
 ; output:
 ;      top of stack   = first number in version ("xx")
 ;      top of stack-1 = rest of the version string ("xx.xx.xx")
Function ParseVersion
   Exch $R1 ; version
   Push $R2
   Push $R3

   StrCpy $R2 1
   loop:
      StrCpy $R3 $R1 1 $R2
      StrCmp $R3 "." loopend
      StrLen $R3 $R1
      IntCmp $R3 $R2 loopend loopend
      IntOp $R2 $R2 + 1
      Goto loop
   loopend:
   Push $R1
   StrCpy $R1 $R1 $R2
   Exch $R1

   StrLen $R3 $R1
   IntOp $R3 $R3 - $R2
   IntOp $R2 $R2 + 1
   StrCpy $R1 $R1 $R3 $R2

   Push $R1

   Exch 2
   Pop $R3

   Exch 2
   Pop $R2

   Exch 2
   Pop $R1
FunctionEnd

;-----------------------------------------------------------------------------
 ; StrStr
 ; input:
 ;      top of stack   = string to search for (the needle)
 ;      top of stack-1 = string to search in (the haystack)
 ; output:
 ;      top of stack   = replaces with the portion of the string remaining
 ;
 ; Usage:
 ;   Push "this is a long ass string"
 ;   Push "ass"
 ;   Call StrStr
 ;   Pop $R0
 ;  ($R0 at this point is "ass string")

Function StrStr
   Exch $R1 ; st=haystack,old$R1, $R1=needle
   Exch     ; st=old$R1,haystack
   Exch $R2 ; st=old$R1,old$R2, $R2=haystack
   Push $R3
   Push $R4
   Push $R5
   StrLen $R3 $R1
   StrCpy $R4 0
   ; $R1=needle
   ; $R2=haystack
   ; $R3=len(needle)
   ; $R4=cnt
   ; $R5=tmp
   loop:
     StrCpy $R5 $R2 $R3 $R4
     StrCmp $R5 $R1 done
     StrCmp $R5 "" done
     IntOp $R4 $R4 + 1
     Goto loop
   done:
   StrCpy $R1 $R2 "" $R4
   Pop $R5
   Pop $R4
   Pop $R3
   Pop $R2
   Exch $R1
FunctionEnd
