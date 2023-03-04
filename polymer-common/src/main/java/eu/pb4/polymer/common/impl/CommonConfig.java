package eu.pb4.polymer.common.impl;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class CommonConfig {
    public String _c1 = "Keep this one at 0, unless you credit this library in another way";
    public int coreCommandOperatorLevel = 0;
    public String _c2 = "Enabled developer utilities";
    public boolean enableDevTools = false;
    public String _c3 = "Uses simpler about display for /polymer command";
    public boolean minimalisticAbout = false;
    public String _c4 = "Logs warnings while creating template/filter entities";
    public boolean enableTemplateEntityWarnings = true;
    public String _c5 = "Enables logging of more exceptions. Useful when debugging";
    public boolean logAllExceptions = false;
}
