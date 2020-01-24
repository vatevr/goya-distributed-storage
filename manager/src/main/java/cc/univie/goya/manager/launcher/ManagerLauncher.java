package cc.univie.goya.manager.launcher;

import io.vertx.core.impl.launcher.VertxCommandLauncher;

import java.util.Arrays;

public class ManagerLauncher extends VertxCommandLauncher {
  public static void main(String[] args) {

    String[] commands = Arrays.asList("run", VManagerRoot.class.getCanonicalName()).toArray(new String[0]);
    new ManagerLauncher().dispatch(commands);

  }
}
