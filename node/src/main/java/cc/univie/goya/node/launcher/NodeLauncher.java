package cc.univie.goya.node.launcher;

import io.vertx.core.impl.launcher.VertxCommandLauncher;

import java.util.Arrays;

public class NodeLauncher extends VertxCommandLauncher {
  public static void main(String[] args) {
    String[] commands = Arrays.asList("run", VNodeRoot.class.getCanonicalName()).toArray(new String[0]);
    new NodeLauncher().dispatch(commands);
  }
}
