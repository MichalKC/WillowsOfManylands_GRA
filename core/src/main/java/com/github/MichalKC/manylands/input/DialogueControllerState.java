package com.github.MichalKC.manylands.input;

public class DialogueControllerState implements ControllerState {

    private Runnable advanceCallback;

    public void setAdvanceCallback(Runnable callback) {
        this.advanceCallback = callback;
    }

    @Override
    public void keyDown(Command command) {
        if ((command == Command.INTERACT || command == Command.SELECT) && advanceCallback != null) {
            advanceCallback.run();
        }
    }
}
