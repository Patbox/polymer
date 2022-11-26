package eu.pb4.polymer.common.impl;

import java.util.Set;

public class MixinOverrideConfig {
    public String _c1a = "Make sure you know what you are doing! This might cause issues! No, this *WILL* cause issues!";
    public String _c1b = "Why did I even implemented that!? Whatever, just make sure YOU ARE 100% SURE that you are";
    public String _c1c = "disabling correct thing! Because otherwise your server/client WILL CRASH.";
    public Set<String> disabledMixins = Set.of();
}
