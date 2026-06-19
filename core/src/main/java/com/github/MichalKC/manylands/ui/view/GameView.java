package com.github.MichalKC.manylands.ui.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.github.MichalKC.manylands.asset.AtlasAsset;
import com.github.MichalKC.manylands.ui.model.GameViewModel;
import com.github.tommyettinger.textra.TextraLabel;
import com.github.tommyettinger.textra.TypingLabel;

import java.util.Map;

public class GameView extends View<GameViewModel> {

    private static final float INTERACT_HINT_BOTTOM_PAD = 14f;
    private static final float TEXT_AREA_BOTTOM_PAD = 20f;
    private static final float COIN_ICON_SIZE = 25f;
    private static final float COIN_ANIM_FRAME_DURATION = 0.15f;
    private final Group lifeGroup;
    private final HorizontalGroup coinGroup;
    private final Label coinCountLabel;
    private final Label interactHintLabel;
    private final Container<Label> textAreaContainer;
    private final Label textAreaLabel;
    private final Container<Label> talkingAreaContainer;
    private final Label talkingAreaLabel;
    private final Label talkingOptionsLabel;
    private Image coinImage;
    private Animation<TextureRegion> coinAnimation;
    private float coinAnimTime;
    private SkillSlotHudWidget skillHudWidget;
    private static final float SKILL_SLOT_SIZE = 24f;
    private SpecialGaugeWidget specialGaugeWidget;
    private Image headIcon;
    private Texture headIconTexture;
    private HorizontalGroup effectsGroup;
    private Texture healEffectTexture;

    private static final float LIFE_HEART_HEIGHT = 9f;  // slightly larger hearts
    private static final float HUD_PAD = 4f;             // outer margin
    private static final float HEAD_ICON_OVERLAP = 10f;  // more overlap -> bar further left
    private static final float HEART_SPACING = 1f;       // tighter spacing between hearts
    private static final float HEARTS_BAR_TIGHTEN = 5f;  // pull bar 5px closer (down) to hearts
    private static final float HEAD_ICON_SCALE = 0.85f;  // shrink head below total bar+hearts height
    private static final float HEARTS_X_OFFSET = 9f;    // hearts farther to the right vs. bar start

    public GameView(Stage stage, Skin skin, GameViewModel viewModel) {
        super(stage, skin, viewModel);

        this.lifeGroup = findActor("lifeGroup");
        this.coinGroup = findActor("coinGroup");
        this.coinCountLabel = findActor("coinCountLabel");
        this.interactHintLabel = findActor("interactHintLabel");
        this.textAreaContainer = findActor("textAreaContainer");
        this.textAreaLabel = findActor("textAreaLabel");
        this.talkingAreaContainer = findActor("talkingAreaContainer");
        this.talkingAreaLabel = findActor("talkingAreaLabel");
        this.talkingOptionsLabel = findActor("talkingOptionsLabel");
        updateLife(viewModel.getLifePoints());
        updateCoins(viewModel.getCoins());
        updateInteractHint(viewModel.isInteractHintVisible());
        updateTextDisplay(viewModel.getTextDisplay());
        updateTalkingDisplay(viewModel.getTalkingDisplay());
        updateEffects(null);
    }

    @Override
    protected void setupPropertyChanges() {
        viewModel.onPropertyChange(GameViewModel.LIFE_POINTS, Integer.class, this::updateLife);
        viewModel.onPropertyChange(GameViewModel.PLAYER_DAMAGE, Map.Entry.class, this::showDamage);
        viewModel.onPropertyChange(GameViewModel.INTERACT_HINT, Boolean.class, this::updateInteractHint);
        viewModel.onPropertyChange(GameViewModel.COINS, Integer.class, this::updateCoins);
        viewModel.onPropertyChange(GameViewModel.TEXT_DISPLAY, String.class, this::updateTextDisplay);
        viewModel.onPropertyChange(GameViewModel.TALKING_DISPLAY, String.class, this::updateTalkingDisplay);
        viewModel.onPropertyChange(GameViewModel.TALKING_OPTIONS, String.class, this::updateTalkingOptions);
        viewModel.onPropertyChange(GameViewModel.SELECTED_SKILL, Integer.class, this::updateSelectedSkill);
        viewModel.onPropertyChange(GameViewModel.ACTIVE_EFFECTS, Object.class, this::updateEffects);
    }

    @Override
    protected void setupUI() {
        align(Align.bottomLeft);
        setFillParent(true);

        HorizontalGroup coinHorizontalGroup = new HorizontalGroup();
        coinHorizontalGroup.setName("coinGroup");
        coinHorizontalGroup.padLeft(0f);
        coinHorizontalGroup.padBottom(0f);
        coinHorizontalGroup.space(2.0f);
        coinHorizontalGroup.align(Align.left | Align.bottom);

        coinAnimation = buildCoinAnimation();
        if (coinAnimation != null) {
            TextureRegion firstFrame = coinAnimation.getKeyFrame(0f);
            TextureRegionDrawable coinDrawable = new TextureRegionDrawable(firstFrame);
            coinDrawable.setMinWidth(COIN_ICON_SIZE);
            coinDrawable.setMinHeight(COIN_ICON_SIZE);
            coinImage = new Image(coinDrawable);
            coinImage.setSize(COIN_ICON_SIZE, COIN_ICON_SIZE);
            coinHorizontalGroup.addActor(coinImage);
        }
        Label coinLabel = new Label("0", skin, "small");
        coinLabel.setName("coinCountLabel");
        coinLabel.setFontScale(0.5f);
        coinLabel.getStyle().font.setUseIntegerPositions(true);
        Container<Label> coinLabelContainer = new Container<>(coinLabel);
        coinLabelContainer.padTop(10f);
        coinLabelContainer.padLeft(-5f);
        coinHorizontalGroup.addActor(coinLabelContainer);
        add(coinHorizontalGroup).left().padBottom(4f);
        row();

        Group horizontalGroup = new Group();
        horizontalGroup.setName("lifeGroup");
        addActor(horizontalGroup);

        Label hint = new Label("(E)", skin, "interactHint");
        hint.setName("interactHintLabel");
        hint.setAlignment(Align.center);
        hint.getStyle().font.setUseIntegerPositions(true);
        hint.setVisible(false);
        hint.pack();
        addActor(hint);

        Label textLabel = new Label("", skin, "small");
        textLabel.setName("textAreaLabel");
        textLabel.setFontScale(0.5f);
        textLabel.setWrap(true);
        textLabel.setAlignment(Align.center);

        Container<Label> textContainer = new Container<>(textLabel);
        textContainer.setName("textAreaContainer");
        Texture textAreaTex = new Texture(Gdx.files.internal("ui/mapThings/textArea.9.png"));
        com.badlogic.gdx.graphics.g2d.TextureRegion textAreaRegion = new com.badlogic.gdx.graphics.g2d.TextureRegion(
            textAreaTex, 1, 1, textAreaTex.getWidth() - 2, textAreaTex.getHeight() - 2);
        NinePatch textAreaPatch = new NinePatch(textAreaRegion, 4, 4, 4, 4);
        textContainer.setBackground(new NinePatchDrawable(textAreaPatch));
        textContainer.pad(6f, 3f, 6f, 3f);
        textContainer.fillX();
        textContainer.setVisible(false);
        addActor(textContainer);

        Label talkingLabel = new Label("", skin, "small");
        talkingLabel.setName("talkingAreaLabel");
        talkingLabel.setFontScale(0.5f);
        talkingLabel.setWrap(true);
        talkingLabel.setAlignment(Align.center);

        Container<Label> talkingContainer = new Container<>(talkingLabel);
        talkingContainer.setName("talkingAreaContainer");
        Texture talkingAreaTex = new Texture(Gdx.files.internal("ui/mapThings/talkingArea.9.png"));
        com.badlogic.gdx.graphics.g2d.TextureRegion talkingAreaRegion = new com.badlogic.gdx.graphics.g2d.TextureRegion(
            talkingAreaTex, 1, 1, talkingAreaTex.getWidth() - 2, talkingAreaTex.getHeight() - 2);
        NinePatch talkingAreaPatch = new NinePatch(talkingAreaRegion, 4, 4, 4, 4);
        talkingContainer.setBackground(new NinePatchDrawable(talkingAreaPatch));
        talkingContainer.pad(6f, 3f, 6f, 3f);
        talkingContainer.fillX();
        talkingContainer.setVisible(false);
        addActor(talkingContainer);

        Label optionsLabel = new Label("", skin, "small");
        optionsLabel.setName("talkingOptionsLabel");
        optionsLabel.setFontScale(0.5f);
        optionsLabel.setAlignment(Align.center);
        optionsLabel.setVisible(false);
        addActor(optionsLabel);

        skillHudWidget = new SkillSlotHudWidget();
        skillHudWidget.setSize(SKILL_SLOT_SIZE, SKILL_SLOT_SIZE);
        if (viewModel.getSelectedSkillHudIdx() >= 0) {
            skillHudWidget.setSkillIcon(viewModel.getSelectedSkillHudIdx());
        }
        int savedOrdinal = viewModel.getSkillWidgetStateOrdinal();
        float savedTimer = viewModel.getSkillWidgetTimer();
        SkillSlotHudWidget.SkillState[] skillStates = SkillSlotHudWidget.SkillState.values();
        if (savedOrdinal > 0 && savedOrdinal < skillStates.length) {
            skillHudWidget.restoreState(skillStates[savedOrdinal], savedTimer);
        }
        addActor(skillHudWidget);

        specialGaugeWidget = new SpecialGaugeWidget();
        specialGaugeWidget.setViewModel(viewModel);
        addActor(specialGaugeWidget);

        try {
            headIconTexture = new Texture(Gdx.files.internal("ui/inventory/headIcon.png"));
            headIcon = new Image(headIconTexture);
            headIcon.setName("headIcon");
            addActor(headIcon);
        } catch (Exception ignored) {}

        effectsGroup = new HorizontalGroup();
        effectsGroup.setName("effectsGroup");
        effectsGroup.space(2f);
        effectsGroup.align(Align.left);
        addActor(effectsGroup);

        try {
            healEffectTexture = new Texture(Gdx.files.internal("ui/inventory/fa1110.png"));
        } catch (Exception ignored) {}
    }

    private void layoutInteractHintLabel() {
        float w = interactHintLabel.getWidth();
        float x = MathUtils.round((getWidth() - w) * 0.5f);
        float y = MathUtils.round(INTERACT_HINT_BOTTOM_PAD);
        interactHintLabel.setPosition(x, y, Align.bottomLeft);
    }

    private void layoutTextArea() {
        float maxWidth = getWidth() * 0.6f;
        float padH = 6f;
        float padV = 12f;

        textAreaLabel.setWrap(false);
        float naturalWidth = textAreaLabel.getPrefWidth() + padH;
        textAreaLabel.setWrap(true);

        float minWidth = getWidth() * 0.3f;
        float width = Math.min(maxWidth, Math.max(minWidth, naturalWidth));
        textAreaLabel.setWidth(width - padH);
        textAreaLabel.layout();
        float height = textAreaLabel.getPrefHeight() + padV;
        textAreaContainer.setSize(width, height);
        textAreaContainer.setPosition((getWidth() - width) / 2f, TEXT_AREA_BOTTOM_PAD);
    }

    private void layoutTalkingArea() {
        float maxWidth = getWidth() * 0.75f;
        float padH = 6f;
        float padV = 12f;

        talkingAreaLabel.setWrap(false);
        float naturalWidth = talkingAreaLabel.getPrefWidth() + padH;
        talkingAreaLabel.setWrap(true);

        float minWidth = getWidth() * 0.4f;
        float width = Math.min(maxWidth, Math.max(minWidth, naturalWidth));
        talkingAreaLabel.setWidth(width - padH);
        talkingAreaLabel.layout();
        float height = talkingAreaLabel.getPrefHeight() + padV;
        talkingAreaContainer.setSize(width, height);
        talkingAreaContainer.setPosition((getWidth() - width) / 2f, TEXT_AREA_BOTTOM_PAD);
    }

    private void updateTextDisplay(String text) {
        if (text == null || text.isEmpty()) {
            textAreaContainer.setVisible(false);
        } else {
            textAreaLabel.setText(text);
            textAreaContainer.setVisible(true);
            layoutTextArea();
        }
    }

    private void updateTalkingDisplay(String text) {
        if (text == null || text.isEmpty()) {
            talkingAreaContainer.setVisible(false);
            talkingOptionsLabel.setVisible(false);
        } else {
            talkingAreaLabel.setText(text);
            talkingAreaContainer.setVisible(true);
            layoutTalkingArea();
        }
    }

    private void updateTalkingOptions(String text) {
        if (text == null || text.isEmpty()) {
            talkingOptionsLabel.setVisible(false);
        } else {
            talkingOptionsLabel.setText(text);
            talkingOptionsLabel.setVisible(true);
            layoutTalkingOptions();
        }
    }

    private void layoutTalkingOptions() {
        talkingOptionsLabel.pack();
        float x = (getWidth() - talkingOptionsLabel.getWidth()) / 2f;
        float y = talkingAreaContainer.getY() - talkingOptionsLabel.getHeight() - 2f;
        talkingOptionsLabel.setPosition(x, y);
    }

    private void updateInteractHint(boolean visible) {
        interactHintLabel.setVisible(visible);
        if (visible) {
            layoutInteractHintLabel();
        }
    }

    private void layoutSkillHud() {
        if (skillHudWidget != null) {
            skillHudWidget.setPosition(getWidth() - SKILL_SLOT_SIZE - 4f, getHeight() - SKILL_SLOT_SIZE - 4f);
        }
    }

    private void layoutSpecialGauge() {
        if (specialGaugeWidget == null) return;

        float barH = 22f;
        float heartsH = LIFE_HEART_HEIGHT;
        float headH = (barH + heartsH) * HEAD_ICON_SCALE;

        float headX = HUD_PAD;
        float headY = getHeight() - headH - HUD_PAD;
        float headW = 0f;
        if (headIcon != null && headIcon.getDrawable() != null) {
            Drawable d = headIcon.getDrawable();
            float srcW = d.getMinWidth();
            float srcH = d.getMinHeight();
            if (srcH <= 0f) srcH = 1f;
            headW = srcW * (headH / srcH);
            headIcon.setBounds(headX, headY, headW, headH);
        }

        float barX = (headIcon != null) ? (headX + headW - HEAD_ICON_OVERLAP) : HUD_PAD;
        float barY = headY + heartsH - HEARTS_BAR_TIGHTEN;
        float maxW = getWidth() - barX - 6f;
        float w = Math.max(40f, Math.min(60f, maxW));
        specialGaugeWidget.setBounds(barX, barY, w, barH);

        layoutHearts(barX + HEARTS_X_OFFSET, headY);

        if (effectsGroup != null) {
            float effectsX = barX + w + 2f;
            float effectsY = barY + (barH - LIFE_HEART_HEIGHT) / 2f + 5f;
            effectsGroup.setPosition(effectsX, effectsY);
        }
    }

    private void layoutHearts(float startX, float baseY) {
        if (lifeGroup == null) return;
        lifeGroup.setPosition(startX, baseY);

        float x = 0f;
        float spacing = HEART_SPACING;
        float maxH = LIFE_HEART_HEIGHT;

        // Lay out each heart image side by side with target height
        for (com.badlogic.gdx.scenes.scene2d.Actor a : lifeGroup.getChildren()) {
            if (a instanceof Image) {
                Image img = (Image) a;
                Drawable d = img.getDrawable();
                float iw = d != null ? d.getMinWidth() : img.getWidth();
                float ih = d != null ? d.getMinHeight() : img.getHeight();
                if (ih <= 0f) ih = 1f;
                float scale = maxH / ih;
                float w = iw * scale;
                float h = maxH;
                img.setBounds(x, 0f, w, h);
                x += w + spacing;
            }
        }
        lifeGroup.setSize(x, maxH);
    }

    private void updateEffects(Object ignored) {
        if (effectsGroup == null) return;
        effectsGroup.clear();

        java.util.Set<String> effects = viewModel.getActiveEffects();
        if (effects != null && effects.contains("heal") && healEffectTexture != null) {
            TextureRegionDrawable drawable = new TextureRegionDrawable(new TextureRegion(healEffectTexture));
            float iconSize = LIFE_HEART_HEIGHT;
            drawable.setMinWidth(iconSize);
            drawable.setMinHeight(iconSize);
            Image healIcon = new Image(drawable);
            healIcon.setSize(iconSize, iconSize);
            effectsGroup.addActor(healIcon);
        }
    }

    public void activateSkill() {
        if (skillHudWidget != null) skillHudWidget.tryActivate();
    }

    public void pauseSkill(boolean paused) {
        if (skillHudWidget != null) skillHudWidget.setPaused(paused);
    }

    private void updateSelectedSkill(Integer hudIdx) {
        if (skillHudWidget == null) return;
        if (hudIdx == null || hudIdx < 0) {
            skillHudWidget.clearSkillIcon();
        } else {
            skillHudWidget.setSkillIcon(hudIdx);
        }
    }

    @Override
    protected void sizeChanged() {
        super.sizeChanged();
        layoutSkillHud();
        layoutSpecialGauge();
        if (interactHintLabel.isVisible()) {
            layoutInteractHintLabel();
        }
        if (textAreaContainer.isVisible()) {
            layoutTextArea();
        }
        if (talkingAreaContainer.isVisible()) {
            layoutTalkingArea();
        }
        if (talkingOptionsLabel.isVisible()) {
            layoutTalkingOptions();
        }
    }

    private Animation<TextureRegion> buildCoinAnimation() {
        TextureAtlas atlas = viewModel.getGame().getAssetService().get(AtlasAsset.OBJECTS);
        Array<TextureAtlas.AtlasRegion> frames = atlas.findRegions("coin/idle_right");
        if (frames.size == 0) {
            frames = atlas.findRegions("coin/drop_right");
        }
        if (frames.size == 0) {
            return null;
        }
        return new Animation<>(COIN_ANIM_FRAME_DURATION, frames, Animation.PlayMode.LOOP);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (skillHudWidget != null) {
            viewModel.saveSkillWidgetState(
                skillHudWidget.getSkillState().ordinal(),
                skillHudWidget.getSkillTimer());
        }
        if (coinAnimation != null && coinImage != null) {
            coinAnimTime += delta;
            TextureRegion frame = coinAnimation.getKeyFrame(coinAnimTime);
            TextureRegionDrawable drawable = new TextureRegionDrawable(frame);
            drawable.setMinWidth(COIN_ICON_SIZE);
            drawable.setMinHeight(COIN_ICON_SIZE);
            coinImage.setDrawable(drawable);
            coinImage.setSize(COIN_ICON_SIZE, COIN_ICON_SIZE);
        }
    }

    private void updateCoins(int coins) {
        if (coinCountLabel != null) {
            coinCountLabel.setText(Integer.toString(Math.max(0, coins)));
        }
    }

    private void updateLife(int lifePoints) {
        lifeGroup.clear();

        int maxLife = viewModel.getMaxLife();
        while (maxLife > 0) {
            int imgIdx = MathUtils.clamp(lifePoints, 0, 4);
            Image image = new Image(skin, "life_0" + imgIdx);
            lifeGroup.addActor(image);

            maxLife -= 4;
            lifePoints -= 4;
        }
        layoutSpecialGauge();
    }

    private Vector2 toStageCoords(Vector2 gamePosition) {
        Vector2 resultPosition = viewModel.toScreenCoords(gamePosition);
        stage.getViewport().unproject(resultPosition);
        resultPosition.y = stage.getViewport().getWorldHeight() - resultPosition.y;
        return resultPosition;
    }

    private void showDamage(Map.Entry<Vector2, Integer> damAndPos) {
        final Vector2 position = damAndPos.getKey();
        int damage = damAndPos.getValue();

        TextraLabel textraLabel = new TypingLabel("[%75]{JUMP=2.0;0.5;0.9}{RAINBOW}" + damage, skin, "small");
        stage.addActor(textraLabel);

        textraLabel.addAction(
            Actions.parallel(
                Actions.sequence(Actions.delay(1.25f), Actions.removeActor()),
                Actions.forever(Actions.run(() -> {
                    Vector2 stageCoords = toStageCoords(position);
                    textraLabel.setPosition(stageCoords.x, stageCoords.y);
                }))
            )
        );
    }
}
