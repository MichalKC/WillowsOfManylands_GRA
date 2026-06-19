package com.github.MichalKC.manylands.ui.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.github.MichalKC.manylands.ui.model.GameViewModel;

public class SpecialGaugeWidget extends Actor {

    private static final float FILL_LERP_SPEED = 2.5f;
    private static final float DRAIN_DURATION_SEC = 1.8f;

    private static final float BAR_HEIGHT = 22f;
    private static final float PULSE_SPEED = 1.2f; // cycles per second while pulsing

    private GameViewModel viewModel;
    private Texture backTex;
    private Texture fillTex;
    private TextureRegion backRegion;
    private TextureRegion fillRegion;
    private NinePatch backPatch;
    private NinePatch fillPatch;

    private float visualFrac = 0f;
    private float pulseT = 0f;
    private boolean pulsing = false;
    private float pulseAlpha = 1f;

    public SpecialGaugeWidget() {
        try {
            backTex = new Texture(Gdx.files.internal("ui/inventory/specialLoadingBack.9.png"));
            backRegion = new TextureRegion(backTex, 1, 1, backTex.getWidth() - 2, backTex.getHeight() - 2);
            backPatch = new NinePatch(backRegion, 7, 7, 12, 12);
            float s = BAR_HEIGHT / backRegion.getRegionHeight();
            backPatch.scale(s, s);
        } catch (Exception ignored) {}
        try {
            fillTex = new Texture(Gdx.files.internal("ui/inventory/specialLoading.9.png"));
            fillRegion = new TextureRegion(fillTex, 1, 1, fillTex.getWidth() - 2, fillTex.getHeight() - 2);
            fillPatch = new NinePatch(fillRegion, 7, 7, 12, 17);
            float s = BAR_HEIGHT / fillRegion.getRegionHeight();
            fillPatch.scale(s, s);
        } catch (Exception ignored) {}
    }

    public void setViewModel(GameViewModel vm) {
        this.viewModel = vm;
        if (vm != null) {
            visualFrac = clamp01(vm.getSpecialCharge());
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (viewModel == null) return;
        float cur = clamp01(viewModel.getSpecialCharge());
        if (viewModel.isSpecialDraining()) {
            float drainRate = 1f / Math.max(0.01f, DRAIN_DURATION_SEC);
            cur = Math.max(0f, cur - drainRate * delta);
            viewModel.setSpecialCharge(cur);
            if (cur <= 0f) {
                viewModel.setSpecialDraining(false);
            }
        } else if (viewModel.isSpecialAlwaysUsable()) {
            if (!pulsing) { pulsing = true; pulseT = 0f; pulseAlpha = 1f; }
            // Visual pulse alpha starting from full (1 -> 0 -> 1) using cosine
            pulseT += delta * PULSE_SPEED;
            pulseAlpha = (float)Math.cos(pulseT * Math.PI * 2) * 0.5f + 0.5f; // 1 at t=0
        } else {
            if (pulsing) { pulsing = false; pulseT = 0f; pulseAlpha = 1f; }
            // Snap immediately on first non-zero to avoid delayed first pixel
            if (visualFrac <= 0.0001f && cur > 0f) {
                visualFrac = cur;
            } else if (cur >= 0.9999f) {
                visualFrac = 1f;
            } else if (visualFrac < cur) {
                visualFrac = Math.min(cur, visualFrac + FILL_LERP_SPEED * delta);
            } else if (visualFrac > cur) {
                visualFrac = Math.max(cur, visualFrac - FILL_LERP_SPEED * delta);
            }
        }
        if (viewModel.isSpecialDraining()) visualFrac = cur;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (backPatch == null || fillPatch == null || backRegion == null || fillRegion == null) return;

        float x = getX();
        float y = getY();
        float targetW = Math.max(1f, getWidth());
        float targetH = Math.max(1f, getHeight());

        float dx = x;
        float dy = y;

        backPatch.draw(batch, dx, dy, targetW, targetH);

        boolean pulse = viewModel != null && viewModel.isSpecialAlwaysUsable();
        float progress = pulse ? 1f : Math.max(0f, Math.min(1f, visualFrac));
        float fw = progress * targetW;

        if (fw > 0f) {
            com.badlogic.gdx.graphics.Color old = new com.badlogic.gdx.graphics.Color(batch.getColor());
            float alpha = pulse ? pulseAlpha : 1f;
            batch.setColor(old.r, old.g, old.b, old.a * alpha);
            boolean fullWidth = progress >= 0.999f;
            if (fullWidth) {
                // Draw without scissor to avoid precision gaps at the end
                fillPatch.draw(batch, dx, dy, targetW, targetH);
            } else {
                // Draw full fillPatch but clip it to the exact progress width using ScissorStack
                // Important: flush previous draws so the backPatch isn't retroactively clipped
                batch.flush();
                // Ensure at least 1px shows for small progress
                float fwDraw = Math.max(1f, (float)Math.ceil(fw));
                Rectangle clip = new Rectangle(dx, dy, fwDraw, targetH);
                Rectangle scissor = new Rectangle();
                ScissorStack.calculateScissors(getStage().getCamera(), batch.getTransformMatrix(), clip, scissor);

                boolean pushed = ScissorStack.pushScissors(scissor);
                try {
                    // Draw full width; only the clipped region [dx, dx+fw] will be visible
                    fillPatch.draw(batch, dx, dy, targetW, targetH);
                } finally {
                    if (pushed) {
                        batch.flush();
                        ScissorStack.popScissors();
                    }
                }
            }
            batch.setColor(old);
        }
    }

    public void dispose() {
        if (backTex != null) { backTex.dispose(); backTex = null; }
        if (fillTex != null) { fillTex.dispose(); fillTex = null; }
    }

    private static float clamp01(float v) { return Math.max(0f, Math.min(1f, v)); }
}
