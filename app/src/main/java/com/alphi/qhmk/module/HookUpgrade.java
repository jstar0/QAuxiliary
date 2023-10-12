package com.alphi.qhmk.module;


import androidx.annotation.NonNull;
import cc.ioctl.util.HookUtils;
import io.github.qauxv.base.annotation.FunctionHookEntry;
import io.github.qauxv.base.annotation.UiItemAgentEntry;
import io.github.qauxv.dsl.FunctionEntryRouter;
import io.github.qauxv.hook.CommonSwitchFunctionHook;
import io.github.qauxv.util.Initiator;
import io.github.qauxv.util.SyncUtils;
import java.lang.reflect.Method;

/**
 * IDEA 2022/1/9
 */

@UiItemAgentEntry
@FunctionHookEntry
public class HookUpgrade  extends CommonSwitchFunctionHook {

    public static final HookUpgrade INSTANCE = new HookUpgrade();

    private HookUpgrade() {
        super(SyncUtils.PROC_MAIN | SyncUtils.PROC_TOOL);
    }

    @NonNull
    @Override
    public String[] getUiItemLocation() {
        return FunctionEntryRouter.Locations.Simplify.UI_MISC;
    }

    @Override
    protected boolean initOnce() throws Exception {
        Class<?> configHandlerClass = Initiator.loadClass("com.tencent.mobileqq.app.ConfigHandler");
        for (Method m : configHandlerClass.getDeclaredMethods()) {
            Class<?>[] parameterTypes = m.getParameterTypes();
            if (m.getReturnType() == void.class && parameterTypes.length == 1 && parameterTypes[0].getSimpleName().equals("UpgradeDetailWrapper")) {
                HookUtils.hookBeforeIfEnabled(this, m, param -> param.setResult(null));
            }
        }

        Class<?> upgc;
        upgc = Initiator.load("com.tencent.mobileqq.upgrade.UpgradeController");
        if (upgc == null) {
            upgc = Initiator.load("com.tencent.mobileqq.app.upgrade.UpgradeController");
        }
        if (upgc == null) {
            throw new RuntimeException("HookUpgrade: UpgradeController not found");
        }

        for (Method m : upgc.getDeclaredMethods()) {
            if (m.getReturnType() == void.class) {
                HookUtils.hookBeforeIfEnabled(this, m, param -> param.setResult(null));
            } else if (m.getReturnType() == boolean.class) {
                HookUtils.hookBeforeIfEnabled(this, m, param -> param.setResult(false));
            }
        }

        return false;
    }

    @NonNull
    @Override
    public String getName() {
        return "屏蔽更新";
    }
}
