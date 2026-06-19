package com.github.MichalKC.manylands.ui.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class SkillSlotHudWidget extends Actor {

    public enum SkillState { READY, ACTIVE, COOLDOWN }

    private static final float ACTIVE_DURATION  = 10f;
    private static final float COOLDOWN_DURATION = 30f;

    private static final float ICON_PAD = 0.09f;

    private SkillState state = SkillState.READY;
    private float stateTimer = 0f;
    private boolean paused = false;

    private Texture frameTexture;
    private Texture iconTexture;
    private final ShapeRenderer shapeRenderer;

    public SkillSlotHudWidget() {
        shapeRenderer = new ShapeRenderer();
        try {
            frameTexture = new Texture(Gdx.files.internal("ui/inventory/skilliconslothudempty2.png"));
        } catch (Exception ignored) {}
    }

    public void setSkillIcon(int hudIdx) {
        disposeIcon();
        if (hudIdx >= 0) {
            try {
                iconTexture = new Texture(Gdx.files.internal("ui/inventory/skilliconslothud" + hudIdx + ".png"));
            } catch (Exception ignored) {}
        }
    }

    public void clearSkillIcon() {
        disposeIcon();
        state = SkillState.READY;
        stateTimer = 0f;
    }

    public void tryActivate() {
        if (state == SkillState.READY && iconTexture != null) {
            state = SkillState.ACTIVE;
            stateTimer = 0f;
        }
    }

    public boolean isReady() {
        return state == SkillState.READY;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public SkillState getSkillState() { return state; }
    public float getSkillTimer()      { return stateTimer; }

    public void restoreState(SkillState s, float timer) {
        this.state = s;
        this.stateTimer = timer;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (paused) return;
        if (state == SkillState.ACTIVE) {
            stateTimer += delta;
            if (stateTimer >= ACTIVE_DURATION) {
                stateTimer = 0f;
                state = SkillState.COOLDOWN;
            }
        } else if (state == SkillState.COOLDOWN) {
            stateTimer += delta;
            if (stateTimer >= COOLDOWN_DURATION) {
                stateTimer = 0f;
                state = SkillState.READY;
            }
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float x = getX();
        float y = getY();
        float w = getWidth();
        float h = getHeight();

        if (frameTexture != null) {
            batch.draw(frameTexture, x, y, w, h);
        }

        float pad = w * ICON_PAD;
        float iconX = x + pad;
        float iconY = y + pad;
        float iconW = w - pad * 2f;
        float iconH = h - pad * 2f;

        if (iconTexture != null) {
            batch.draw(iconTexture, iconX, iconY, iconW, iconH);
        }

        float sweep = getSweepAngle();
        if (sweep > 0.5f && iconTexture != null) {
            batch.end();

            Gdx.gl.glEnable(GL20.GL_STENCIL_TEST);
            Gdx.gl.glClear(GL20.GL_STENCIL_BUFFER_BIT);
            Gdx.gl.glStencilMask(0xFF);
            Gdx.gl.glStencilFunc(GL20.GL_ALWAYS, 1, 0xFF);
            Gdx.gl.glStencilOp(GL20.GL_KEEP, GL20.GL_KEEP, GL20.GL_REPLACE);
            Gdx.gl.glColorMask(false, false, false, false);

            shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
            shapeRenderer.setTransformMatrix(batch.getTransformMatrix());
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(1f, 1f, 1f, 1f);
            float cx = iconX + iconW / 2f;
            float cy = iconY + iconH / 2f;
            float radius = (float) Math.sqrt(iconW * iconW + iconH * iconH);

            shapeRenderer.arc(cx, cy, radius, 90f, -sweep, 64);
            shapeRenderer.end();

            // Pass 2: redraw the icon at dark tint, only where stencil = 1
            Gdx.gl.glColorMask(true, true, true, true);
            Gdx.gl.glStencilMask(0x00);
            Gdx.gl.glStencilFunc(GL20.GL_EQUAL, 1, 0xFF);
            Gdx.gl.glStencilOp(GL20.GL_KEEP, GL20.GL_KEEP, GL20.GL_KEEP);

            batch.begin();
            batch.setColor(0.3f, 0.3f, 0.3f, 1f);
            batch.draw(iconTexture, iconX, iconY, iconW, iconH);
            batch.flush();
            batch.setColor(Color.WHITE);

            // Restore default GL state
            Gdx.gl.glStencilMask(0xFF);
            Gdx.gl.glDisable(GL20.GL_STENCIL_TEST);
        }
    }

    private float getSweepAngle() {
        if (state == SkillState.ACTIVE) {
            return Math.min((stateTimer / ACTIVE_DURATION) * 360f, 360f);
        } else if (state == SkillState.COOLDOWN) {
            return Math.max((1f - stateTimer / COOLDOWN_DURATION) * 360f, 0f);
        }
        return 0f;
    }

    private void disposeIcon() {
        if (iconTexture != null) {
            iconTexture.dispose();
            iconTexture = null;
        }
    }

    public void dispose() {
        shapeRenderer.dispose();
        if (frameTexture != null) { frameTexture.dispose(); frameTexture = null; }
        disposeIcon();
    }
}
