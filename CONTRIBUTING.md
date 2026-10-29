# Some general rules / request when creating pull requests and issue reports for Polymer library set

## AI generated code, text and assets are not allowed.
If you want to pr something, only code written without AI help will be allowed. 

If you just wanted to fix issue you found, rather than using AI to do that just create an issue for it.

In case of issue reports don't use AI to analyze things and just report what doesn't work directly.

Polymer is a passion project first and foremost and I want to keep it that way, while generative AI just poisons that 
(and as a thing to note code-AI does count as generative AI).

## When creating issues.
Try to report everything relevant you know what mods and versions are you using (Minecraft, Fabric, etc).
Logs should be uploaded as files.

## When creating pull requests.
All new additions should target currently default / latest branch, as that's where the main development
is happening. In case of backports feel free to pr to relevant branches directly.

Avoid reformatting files. Keep the binary compatibility for api stuff. Using var is fine. 
Code style used is whatever default IntelliJ formatter is, so just keep it consistent with other files.

Naturally respect the license, as Polymer is LGPLv3 so anything added needs to be license-able under that.