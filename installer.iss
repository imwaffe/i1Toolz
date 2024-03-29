; Script generated by the Inno Setup Script Wizard.
; SEE THE DOCUMENTATION FOR DETAILS ON CREATING INNO SETUP SCRIPT FILES!

#define MyAppName "i1Toolz"
#define MyAppVersion "0.3.2"
#define MyAppPublisher "Luca Armellin"
#define MyAppURL "https://www.armellinluca.com/"
#define MyAppExeName "i1Toolz.exe"
#define MyAppIcoName "i1Toolz.ico"
#define MyAppAssocName MyAppName + " Project"
#define MyAppAssocExt ".i1tz"
#define MyAppAssocKey StringChange(MyAppAssocName, " ", "") + MyAppAssocExt

[Setup]
; NOTE: The value of AppId uniquely identifies this application. Do not use the same AppId value in installers for other applications.
; (To generate a new GUID, click Tools | Generate GUID inside the IDE.)
; ACTUAL AppId={{9E2A8B2D-55A4-4963-877E-864D10D22BBF}
AppId = {{DC673D0D-B071-4D6F-9C3D-99C342F1B8A6}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
;AppVerName={#MyAppName} {#MyAppVersion}
AppPublisher={#MyAppPublisher}
AppPublisherURL={#MyAppURL}
AppSupportURL={#MyAppURL}
AppUpdatesURL={#MyAppURL}
DefaultDirName={autopf}\{#MyAppName}
ChangesAssociations=yes
DisableProgramGroupPage=yes
; Uncomment the following line to run in non administrative install mode (install for current user only.)
;PrivilegesRequired=lowest
PrivilegesRequiredOverridesAllowed=dialog
OutputBaseFilename=i1Toolz_0.3.2_Installer
Compression=lzma
SolidCompression=yes
WizardStyle=modern
LicenseFile = "installer\license.txt"

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Files]
Source: "C:\users\lucaa\Documents\JAVA\i1Toolz\out\artifacts\i1Toolz\bundles\i1Toolz\{#MyAppExeName}"; DestDir: "{app}"; Flags: ignoreversion
Source: "C:\users\lucaa\Documents\JAVA\i1Toolz\out\artifacts\i1Toolz\bundles\i1Toolz\i1Toolz.ico"; DestDir: "{app}"; Flags: ignoreversion
Source: "C:\users\lucaa\Documents\JAVA\i1Toolz\out\artifacts\i1Toolz\bundles\i1Toolz\msvcp140.dll"; DestDir: "{app}"; Flags: ignoreversion
Source: "C:\users\lucaa\Documents\JAVA\i1Toolz\out\artifacts\i1Toolz\bundles\i1Toolz\msvcp140_1.dll"; DestDir: "{app}"; Flags: ignoreversion
Source: "C:\users\lucaa\Documents\JAVA\i1Toolz\out\artifacts\i1Toolz\bundles\i1Toolz\msvcp140_2.dll"; DestDir: "{app}"; Flags: ignoreversion
Source: "C:\users\lucaa\Documents\JAVA\i1Toolz\out\artifacts\i1Toolz\bundles\i1Toolz\packager.dll"; DestDir: "{app}"; Flags: ignoreversion
Source: "C:\users\lucaa\Documents\JAVA\i1Toolz\out\artifacts\i1Toolz\bundles\i1Toolz\vcruntime140.dll"; DestDir: "{app}"; Flags: ignoreversion
Source: "C:\users\lucaa\Documents\JAVA\i1Toolz\out\artifacts\i1Toolz\bundles\i1Toolz\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "C:\users\lucaa\Documents\JAVA\i1Toolz\dll\*"; DestDir: "{sys}"; Flags: 32bit
; NOTE: Don't use "Flags: ignoreversion" on any shared system files

[Registry]
Root: HKA; Subkey: "Software\Classes\{#MyAppAssocExt}\OpenWithProgids"; ValueType: string; ValueName: "{#MyAppAssocKey}"; ValueData: ""; Flags: uninsdeletevalue
Root: HKA; Subkey: "Software\Classes\{#MyAppAssocKey}"; ValueType: string; ValueName: ""; ValueData: "{#MyAppAssocName}"; Flags: uninsdeletekey
Root: HKA; Subkey: "Software\Classes\{#MyAppAssocKey}\DefaultIcon"; ValueType: string; ValueName: ""; ValueData: "{app}\{#MyAppExeName},0"
Root: HKA; Subkey: "Software\Classes\{#MyAppAssocKey}\shell\open\command"; ValueType: string; ValueName: ""; ValueData: """{app}\{#MyAppExeName}"" ""%1"""
Root: HKA; Subkey: "Software\Classes\Applications\{#MyAppExeName}\SupportedTypes"; ValueType: string; ValueName: ".myp"; ValueData: ""

[Icons]
Name: "{autoprograms}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"
Name: "{autodesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; IconFilename: "{app}\{#MyAppIcoName}"; Tasks: desktopicon

[Run]
Filename: "{app}\{#MyAppExeName}"; Description: "{cm:LaunchProgram,{#StringChange(MyAppName, '&', '&&')}}"; Flags: nowait postinstall skipifsilent; Check: EyeOneDllExists          
;Filename: "{app}\{#MyAppExeName}"; Description: "{cm:LaunchProgram,{#StringChange(MyAppName, '&', '&&')}}"; Flags: nowait postinstall skipifsilent

[Code]
var
  EyeOneDllExistsPage: TWizardPage;
  DownloadSoftwareButton: TButton;
  DownloadI1Match: TDownloadWizardPage;

function EyeOneDllExists: Boolean;
begin
  Result := FileExists(ExpandConstant('{sys}\EyeOne.dll'));
end;

procedure DownloadXriteSoftware(Sender: TObject);
var
ResultCode: Integer;
copyres: Boolean;
begin
  try
    DownloadI1Match.Show;
    DownloadI1Match.Download
  except
    Log(GetExceptionMessage);
    DownloadI1Match.Hide;
  finally
    Exec(ExpandConstant('{tmp}\i1Match_3.6.2_Win7.exe'), '', '', SW_SHOW, ewWaitUntilTerminated, ResultCode);
    FileCopy(ExpandConstant('{commonpf32}\GretagMacbeth\i1\Eye-One Match 3\EyeOne.dll'), ExpandConstant('{sys}\EyeOne.dll'), False);
    EyeOneDllExists
    DownloadI1Match.Hide;
  end;
end;

procedure InitializeWizard();
begin      
  EyeOneDllExistsPage := CreateOutputMsgPage(wpSelectTasks,
  'EyeOne software missing', 'X-Rite EyeOne driver and/or DLL are missing.',
  'In order to use i1Toolz together with an X-Rite EyeOne instrument, you should download and install the X-Rite EyeOne drivers and DLLs from their website. You can install any piece of software. In order to attempt to download and install X-Rite i1Match software, you can click on the "Download i1Match" button and follow the wizard, please DO NOT CHANGE THE DEFAULT INSTALLATION DIRECTORY or i1Toolz installer will fail to find the required files. THIS INSTALLER DOES NOT DISTRIBUTE ANY X-RITE SOFTWARE, IT SIMPLY DOWNLOADS THE INSTALLER FROM THEIR WEBSITE AND OPENS IT UP FOR THE USER TO CONTINUE THE INSTALLATION, IF WILLING TO DO SO. X-RITE AND EYEONE ARE COPYRIGHTED AND ALL THE RIGHTS BELONG TO X-RITE AND PANTONE.');   
  DownloadSoftwareButton := TButton.Create(WizardForm);
  DownloadSoftwareButton.Parent := WizardForm;
  DownloadSoftwareButton.Caption := 'Download i1Match';
  DownloadSoftwareButton.Left := 30;
  DownloadSoftwareButton.Top := WizardForm.NextButton.Top;
  DownloadSoftwareButton.Width := 150;
  DownloadSoftwareButton.OnClick := @DownloadXriteSoftware;

  DownloadI1Match := CreateDownloadPage('Downloading i1Match Installer', 'An attempt to download the X-Rite i1Match Installer from the X-Rite website is underway.', nil);
  DownloadI1Match.Add('https://downloads.xrite.com/downloads/software/GMB/Eye-One_Match/v3.6.2/i1Match_3.6.2_Win7.exe','i1Match_3.6.2_Win7.exe','');

  FileCopy(ExpandConstant('{commonpf32}\GretagMacbeth\i1\Eye-One Match 3\EyeOne.dll'), ExpandConstant('{sys}\EyeOne.dll'), False);
end;

procedure CurPageChanged(CurPageID: Integer);
begin
  // make your button visible only when the page that is just to be displayed is the components page
  DownloadSoftwareButton.Visible := CurPageID = EyeOneDllExistsPage.ID;
end;

function ShouldSkipPage(PageID: Integer): Boolean;
begin
  if PageID = EyeOneDllExistsPage.ID then
    begin
      Result := EyeOneDllExists;
    end; 
end;
