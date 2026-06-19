package com.github.MichalKC.manylands.ui.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Scaling;
import com.github.MichalKC.manylands.ui.model.MenuViewModel;

public class MenuView extends View<MenuViewModel> {

    private final Image selectionImg;
    private Group selectedItem;
    private ParallaxLayer skyLayer, moonLayer, mountainLayer, backLayer, midLayer, longLayer, shortLayer;
    private Array<Texture> ownedTextures;
    private Image bannerImage;
    private com.badlogic.gdx.scenes.scene2d.ui.Cell<Image> bannerCell;
    private float bannerWidth;
    private float bannerHeight;
    private Label footerLabel;
    private com.badlogic.gdx.scenes.scene2d.ui.Cell<Label> footerCell;
    private Table contentTable;
    private MenuPage currentPage;

    public MenuView(Stage stage, Skin skin, MenuViewModel viewModel) {
        super(stage, skin, viewModel);

        this.selectionImg = new Image(skin, "selection");
        this.selectionImg.setTouchable(Touchable.disabled);
        this.selectedItem = findActor(MenuOption.START_GAME.name());
        selectMenuItem(this.selectedItem);
    }

    private void selectMenuItem(Group menuItem) {
        if (selectionImg.getParent() != null) {
            selectionImg.remove();
        }
        this.selectedItem = menuItem;

        float extraSize = 7f;
        float halfExtraSize = extraSize * 0.5f;
        float resizeTime = 0.2f;

        if (menuItem instanceof Layout l) {
            l.validate();
        }
        float w = menuItem.getWidth();
        float h = menuItem.getHeight();
        if (w <= 1f || h <= 1f) {
            if (menuItem instanceof Layout l) {
                if (w <= 1f) w = l.getPrefWidth();
                if (h <= 1f) h = l.getPrefHeight();
            }
        }

        menuItem.addActor(selectionImg);
        selectionImg.setPosition(-halfExtraSize, -halfExtraSize);
        selectionImg.setSize(w + extraSize, h + extraSize);
        selectionImg.clearActions();
        selectionImg.addAction(Actions.forever(Actions.sequence(
            Actions.parallel(
                Actions.sizeBy(extraSize, extraSize, resizeTime, Interpolation.linear),
                Actions.moveBy(-halfExtraSize, -halfExtraSize, resizeTime, Interpolation.linear)
            ),
            Actions.parallel(
                Actions.sizeBy(-extraSize, -extraSize, resizeTime, Interpolation.linear),
                Actions.moveBy(halfExtraSize, halfExtraSize, resizeTime, Interpolation.linear)
            )
        )));
    }

    @Override
    protected void setupUI() {
        ownedTextures = new Array<>();
        setFillParent(true);

        // Add background layers from assets/ui/menu
        try {
            Texture skyTexture = new Texture(Gdx.files.internal("ui/menu/forest_sky.png"));
            skyLayer = new ParallaxLayer(skyTexture, 2f);
            addActor(skyLayer);
        } catch (Exception e) {
            // If background not found, continue without it
        }

        try {
            Texture moonTexture = new Texture(Gdx.files.internal("ui/menu/forest_moon.png"));
            moonLayer = new ParallaxLayer(moonTexture, 0f);
            addActor(moonLayer);
        } catch (Exception e) {
            // Continue without moon if not found
        }

        try {
            Texture mountainTexture = new Texture(Gdx.files.internal("ui/menu/forest_mountain.png"));
            mountainLayer = new ParallaxLayer(mountainTexture, 6f);
            addActor(mountainLayer);
        } catch (Exception e) {
            // Continue without mountains if not found
        }

        try {
            Texture backTexture = new Texture(Gdx.files.internal("ui/menu/forest_back.png"));
            backLayer = new ParallaxLayer(backTexture, 10f);
            addActor(backLayer);
        } catch (Exception e) {
            // Continue without back trees if not found
        }

        try {
            Texture midTexture = new Texture(Gdx.files.internal("ui/menu/forest_mid.png"));
            midLayer = new ParallaxLayer(midTexture, 14f);
            addActor(midLayer);
        } catch (Exception e) {
            // Continue without mid trees if not found
        }

        try {
            Texture longTexture = new Texture(Gdx.files.internal("ui/menu/forest_long.png"));
            longLayer = new ParallaxLayer(longTexture, 18f);
            addActor(longLayer);
        } catch (Exception e) {
            // Continue without long trees if not found
        }

        try {
            Texture shortTexture = new Texture(Gdx.files.internal("ui/menu/forest_short.png"));
            shortLayer = new ParallaxLayer(shortTexture, 24f);
            addActor(shortLayer);
        } catch (Exception e) {
            // Continue without short trees if not found
        }

        bannerImage = new Image(skin, "banner");
        bannerImage.setScaling(Scaling.fit);
        float scale = 1.5f;
        this.bannerWidth = 400f * scale;
        this.bannerHeight = 100f * scale;
        bannerCell = add(bannerImage).width(bannerWidth).height(bannerHeight).padTop(20f).padBottom(30f);
        row();

        currentPage = MenuPage.MAIN;
        setupMenuContent();

        footerLabel = new Label("MKC", skin, "small");
        footerLabel.setColor(skin.getColor("white"));
        footerCell = add(footerLabel).padRight(5.0f).padBottom(5f).align(Align.bottomRight);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (skyLayer != null) skyLayer.update(delta);
        if (moonLayer != null) moonLayer.update(delta);
        if (mountainLayer != null) mountainLayer.update(delta);
        if (backLayer != null) backLayer.update(delta);
        if (midLayer != null) midLayer.update(delta);
        if (longLayer != null) longLayer.update(delta);
        if (shortLayer != null) shortLayer.update(delta);
    }

    private static class ParallaxLayer extends Group {
        private final Texture texture;
        private final float speed;
        private float scrollX;

        public ParallaxLayer(Texture texture, float speed) {
            this.texture = texture;
            this.speed = speed;
            this.scrollX = 0f;
            setTouchable(Touchable.disabled);
            texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.ClampToEdge);
        }

        public void update(float delta) {
            scrollX += speed * delta;
            float height = getStage().getViewport().getWorldHeight();
            float scale = height / texture.getHeight();
            float drawWidth = texture.getWidth() * scale;
            while (scrollX >= drawWidth) {
                scrollX -= drawWidth;
            }
        }

        @Override
        public void draw(com.badlogic.gdx.graphics.g2d.Batch batch, float parentAlpha) {
            super.draw(batch, parentAlpha);

            float x = getX() + scrollX;
            float y = getY();
            float width = getStage().getViewport().getWorldWidth();
            float height = getStage().getViewport().getWorldHeight();

            float scale = height / texture.getHeight();
            float drawWidth = texture.getWidth() * scale;

            float drawX = x - drawWidth;
            float overlap = 0.5f;
            while (drawX < width + drawWidth) {
                batch.draw(texture, drawX, y, drawWidth + overlap, height);
                drawX += drawWidth;
            }
        }
    }

    private void setupMenuContent() {
        contentTable = new Table();
        Drawable frameBg = safeNinePatch9("ui/inventory/inventoryQuestText.9.png");
        if (frameBg == null) frameBg = skin.getDrawable("frame");
        contentTable.setBackground(frameBg);
        contentTable.padLeft(40.0f);
        contentTable.padRight(40.0f);
        contentTable.padTop(25.0f);
        contentTable.padBottom(20.0f);
        add(contentTable).center().expand().row();

        rebuildContent();
    }

    private void rebuildContent() {
        contentTable.clearChildren();
        switch (currentPage) {
            case MAIN -> buildMainMenuContent();
            case SETTINGS -> buildSettingsContent();
        }
    }

    private void buildMainMenuContent() {
        TextButton startBtn = new TextButton("Start Game", skin);
        startBtn.setName(MenuOption.START_GAME.name());
        contentTable.add(startBtn);
        onClick(startBtn, viewModel::startGame);
        onEnter(startBtn, this::selectMenuItem);
        contentTable.row();

        TextButton settingsBtn = new TextButton("Settings", skin);
        settingsBtn.setName(MenuOption.SETTINGS.name());
        contentTable.add(settingsBtn).padTop(10.0f).row();
        onClick(settingsBtn, this::showSettingsPage);
        onEnter(settingsBtn, this::selectMenuItem);

        TextButton quitBtn = new TextButton("Quit Game", skin);
        quitBtn.setName(MenuOption.QUIT_GAME.name());
        contentTable.add(quitBtn).padTop(10.0f);
        onClick(quitBtn, viewModel::quitGame);
        onEnter(quitBtn, this::selectMenuItem);
    }

    private void buildSettingsContent() {
        Table listTable = new Table();
        listTable.defaults().padTop(6f);
        contentTable.add(listTable).center().row();

        Slider musicSlider = setupVolumeSlider(listTable, "Music Volume", MenuOption.MUSIC_VOLUME);
        musicSlider.setValue(viewModel.getMusicVolume());
        onChange(musicSlider, (slider) -> viewModel.setMusicVolume(slider.getValue()));

        Slider soundSlider = setupVolumeSlider(listTable, "Sound Volume", MenuOption.SOUND_VOLUME);
        soundSlider.setValue(viewModel.getSoundVolume());
        onChange(soundSlider, (slider) -> viewModel.setSoundVolume(slider.getValue()));

        TextButton backBtn = new TextButton("Back", skin);
        backBtn.setName(MenuOption.BACK.name());
        listTable.add(backBtn).padTop(10.0f).center().row();
        onClick(backBtn, this::showMainPage);
        onEnter(backBtn, this::selectMenuItem);

        // Controls list directly below; frame expands to fit
        addControlsList(contentTable);

        Group first = (Group) musicSlider.getParent();
        selectMenuItem(first);
    }

    private void addControlsList(Table container) {
        Table controls = new Table();
        controls.setTouchable(Touchable.disabled);

        Label header = new Label("Controls", skin);
        header.setColor(skin.getColor("smokyBlue"));
        controls.add(header).left().row();

        controls.add(new Label("Move: W/A/S/D or Arrow Keys", skin)).left().row();
        controls.add(new Label("Interact / Talk: E", skin)).left().row();
        controls.add(new Label("Inventory: I", skin)).left().row();
        controls.add(new Label("Use Skill: L", skin)).left().row();
        controls.add(new Label("Select / Confirm: SPACE", skin)).left().row();
        controls.add(new Label("Cancel / Back: ESC", skin)).left().row();
        controls.add(new Label("Secondary Attack: J", skin)).left().row();
        controls.add(new Label("Special: K", skin)).left().row();
        controls.add(new Label("Combat: ESC = Defeat (respawn)", skin)).left().row();

        container.add(controls).padTop(14.0f).left().row();
    }

    private Slider setupVolumeSlider(Table contentTable, String title, MenuOption menuOption) {
        Table table = new Table();
        table.setName(menuOption.name());
        table.defaults().center();

        Label label = new Label(title, skin);
        label.setColor(skin.getColor("smokyBlue"));
        table.add(label).center().row();

        Slider slider = new Slider(0.0f, 1f, 0.05f, false, skin);
        table.add(slider).center();
        contentTable.add(table).padTop(10.0f).center().row();

        onEnter(table, this::selectMenuItem);
        return slider;
    }

    @Override
    public void onDown() {
        Group menuContentTable = this.selectedItem.getParent();
        int currentIdx = menuContentTable.getChildren().indexOf(this.selectedItem, true);
        if (currentIdx == -1) {
            throw new GdxRuntimeException("'selectedItem' is not a child of 'menuContentTable'");
        }

        int numOptions = menuContentTable.getChildren().size;
        currentIdx = (currentIdx + 1) % numOptions;
        selectMenuItem((Group) menuContentTable.getChild(currentIdx));
    }

    @Override
    public void onUp() {
        Group menuContentTable = this.selectedItem.getParent();
        int currentIdx = menuContentTable.getChildren().indexOf(this.selectedItem, true);
        if (currentIdx == -1) {
            throw new GdxRuntimeException("'selectedItem' is not a child of 'menuContentTable'");
        }

        int numOptions = menuContentTable.getChildren().size;
        currentIdx = currentIdx == 0 ? numOptions - 1 : currentIdx - 1;
        selectMenuItem((Group) menuContentTable.getChild(currentIdx));
    }

    @Override
    public void onRight() {
        MenuOption menuOption = MenuOption.valueOf(this.selectedItem.getName());
        switch (menuOption) {
            case MUSIC_VOLUME, SOUND_VOLUME -> {
                Slider slider = (Slider) this.selectedItem.getChild(1);
                slider.setValue(slider.getValue() + slider.getStepSize());
            }
        }
    }

    @Override
    public void onLeft() {
        MenuOption menuOption = MenuOption.valueOf(this.selectedItem.getName());
        switch (menuOption) {
            case MUSIC_VOLUME, SOUND_VOLUME -> {
                Slider slider = (Slider) this.selectedItem.getChild(1);
                slider.setValue(slider.getValue() - slider.getStepSize());
            }
        }
    }

    @Override
    public void onSelect() {
        MenuOption menuOption = MenuOption.valueOf(this.selectedItem.getName());
        switch (menuOption) {
            case START_GAME -> viewModel.startGame();
            case SETTINGS -> showSettingsPage();
            case BACK -> showMainPage();
            case QUIT_GAME -> viewModel.quitGame();
        }
    }

    @Override
    public void onCancel() {
        if (currentPage == MenuPage.SETTINGS) {
            showMainPage();
        }
    }

    private Drawable safeNinePatch9(String path) {
        try {
            Pixmap pm = new Pixmap(Gdx.files.internal(path));
            int fullW = pm.getWidth();
            int fullH = pm.getHeight();
            int stretchXStart = -1, stretchXEnd = -1;
            for (int x = 1; x < fullW - 1; x++) {
                if (isGuidePixel(pm.getPixel(x, 0))) {
                    if (stretchXStart < 0) stretchXStart = x;
                    stretchXEnd = x;
                }
            }
            int stretchYStart = -1, stretchYEnd = -1;
            for (int y = 1; y < fullH - 1; y++) {
                if (isGuidePixel(pm.getPixel(0, y))) {
                    if (stretchYStart < 0) stretchYStart = y;
                    stretchYEnd = y;
                }
            }
            pm.dispose();
            int pLeft   = stretchXStart > 0 ? stretchXStart - 1 : 4;
            int pRight  = stretchXEnd   > 0 ? (fullW - 2) - stretchXEnd : 4;
            int pTop    = stretchYStart > 0 ? stretchYStart - 1 : 4;
            int pBottom = stretchYEnd   > 0 ? (fullH - 2) - stretchYEnd : 4;
            Texture tex = new Texture(Gdx.files.internal(path));
            ownedTextures.add(tex);
            TextureRegion region = new TextureRegion(tex, 1, 1, fullW - 2, fullH - 2);
            NinePatch np = new NinePatch(region, pLeft, pRight, pTop, pBottom);
            return new NinePatchDrawable(np);
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean isGuidePixel(int rgba) {
        int a = rgba & 0xFF;
        int r = (rgba >> 24) & 0xFF;
        int g = (rgba >> 16) & 0xFF;
        int b = (rgba >> 8) & 0xFF;
        return a > 128 && r < 64 && g < 64 && b < 64;
    }

    private enum MenuOption {
        START_GAME,
        SETTINGS,
        MUSIC_VOLUME,
        SOUND_VOLUME,
        BACK,
        QUIT_GAME
    }

    private enum MenuPage {
        MAIN,
        SETTINGS
    }

    private void showSettingsPage() {
        currentPage = MenuPage.SETTINGS;
        if (bannerCell != null) {
            bannerImage.setVisible(false);
            bannerCell.height(0f);
            bannerCell.padTop(0f).padBottom(0f);
        }
        if (footerCell != null) {
            footerLabel.setVisible(false);
            footerCell.padBottom(0f);
            footerCell.height(0f);
        }
        rebuildContent();
    }

    private void showMainPage() {
        currentPage = MenuPage.MAIN;
        if (bannerCell != null) {
            bannerImage.setVisible(true);
            bannerCell.height(bannerHeight);
            bannerCell.padTop(20f).padBottom(30f);
        }
        stage.setScrollFocus(null);
        if (footerCell != null) {
            footerLabel.setVisible(true);
            footerCell.height(com.badlogic.gdx.scenes.scene2d.ui.Value.prefHeight);
            footerCell.padBottom(5f);
        }
        rebuildContent();
        Group start = findActor(MenuOption.START_GAME.name());
        if (start != null) selectMenuItem(start);
    }
}
