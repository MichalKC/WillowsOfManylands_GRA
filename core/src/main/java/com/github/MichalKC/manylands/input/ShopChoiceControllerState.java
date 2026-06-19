package com.github.MichalKC.manylands.input;

public class ShopChoiceControllerState implements ControllerState {

    private Runnable yesCallback;
    private Runnable noCallback;

    public void setYesCallback(Runnable callback) {
        this.yesCallback = callback;
    }

    public void setNoCallback(Runnable callback) {
        this.noCallback = callback;
    }

    @Override
    public void keyDown(Command command) {
        if ((command == Command.INTERACT || command == Command.SELECT) && yesCallback != null) {
            yesCallback.run();
        } else if (command == Command.CANCEL && noCallback != null) {
            noCallback.run();
        }
    }
}
