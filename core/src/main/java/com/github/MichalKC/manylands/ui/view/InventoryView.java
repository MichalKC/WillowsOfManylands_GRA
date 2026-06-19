package com.github.MichalKC.manylands.ui.view;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.Input;
import java.util.function.IntPredicate;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.github.MichalKC.manylands.asset.AssetService;
import com.github.MichalKC.manylands.asset.AtlasAsset;
import com.github.MichalKC.manylands.component.Storage;
import com.github.MichalKC.manylands.input.Command;
import com.github.MichalKC.manylands.input.UiEvent;
import com.github.MichalKC.manylands.ui.model.InventoryViewModel;

public class InventoryView extends View<InventoryViewModel> {

    private static final float WINDOW_W = 160f;
    private static final float WINDOW_H = 120f;
    private static final float STORAGE_WINDOW_W = 200f;
    private static final float STORAGE_WINDOW_H = 120f;

    private Array<Texture> ownedTextures;
    private Array<ItemSlot> inventorySlots;
    private Array<ItemSlot> storageSlots;
    private Table[] tabPanels;
    private Image[] tabButtons;
    private Image selectionFrame;
    private Image storageSelectionFrame;
    private Label coinLabel;
    private Table coinWrapper;
    private Label[] statLabels;
    private Runnable closeCallback;
    private AssetService assetService;
    private Stack charActorContainer;
    private boolean storageMode;
    private boolean storageSelectionActive;
    private int storageSelectedSlot;
    private int storageRows;
    private int storageCols;
    private Label itemNameLabel;
    private Table itemTooltip;
    private Label itemTooltipLabel;
    private Image[] questButtons;
    private int activeQuestButton = -1;
    private MarkerActor[] markerActors;
    private Array<ItemSlot> equipSlots;
    private Image equipSelectionFrame;
    private boolean equipSelectionActive = false;
    private int equipSelectedSlot = 0;
    private int selectedSkillSlot = 0;
    private Array<Stack> skillSlotActors;
    private static final int[] SKILL_FRAMES_NORMAL   = {1, 10, 2, 13, 12, 3, 15, 14};
    private static final int[] SKILL_FRAMES_SELECTED = {5, 18, 6, 21, 20, 7, 23, 22};
    private static final int[] SKILL_SLOT_ICONS      = {6, 1, 0, 3, 5, 2, 4, 7};
    private static final int[] SKILL_NAV_LEFT  = {0, 0, 2, 2, 3, 5, 5, 6};
    private static final int[] SKILL_NAV_RIGHT = {1, 1, 3, 4, 4, 6, 7, 7};
    private static final int[] SKILL_NAV_UP    = {0, 1, 0, 1, 1, 2, 3, 4};
    private static final int[] SKILL_NAV_DOWN  = {2, 3, 5, 6, 7, 5, 6, 7};
    private static final int[] SKILL_HUD_ICONS  = {3, 4, 0, 5, 7, 1, 2, 6};
    private int activatedSkillSlot;
    private Stack skillPreviewSlot;
    private Label skillDescLabel;
    private java.util.function.Consumer<Integer> skillSelectedCallback;
    private java.util.function.BooleanSupplier skillLockSupplier;
    private java.util.function.Consumer<com.github.MichalKC.manylands.component.Item> eatCallback;
    private Table readingOverlay;
    private Label readingLabel;
    private boolean readingActive = false;
    private ScrollPane readingScroll;

    public InventoryView(Stage stage, Skin skin, InventoryViewModel viewModel) {
        super(stage, skin, viewModel);
        setVisible(false);
    }

    public void setAssetService(AssetService assetService) {
        this.assetService = assetService;
        if (charActorContainer != null) {
            charActorContainer.clearChildren();
            charActorContainer.add(buildWarriorActor());
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (readingActive && readingScroll != null) {
            boolean up = Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W);
            boolean down = Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S);
            if (up || down) {
                float speed = 80f;
                float dy = (down ? speed * delta : 0f) - (up ? speed * delta : 0f);
                readingScroll.setScrollY(readingScroll.getScrollY() + dy);
                readingScroll.updateVisualScroll();
            }
        }
    }

    private static class AnimatedActor extends Actor {
        private final Animation<TextureRegion> animation;
        private float stateTime = 0f;

        AnimatedActor(Animation<TextureRegion> animation) {
            this.animation = animation;
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            stateTime += delta;
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            TextureRegion frame = animation.getKeyFrame(stateTime, true);
            float fw = frame.getRegionWidth();
            float fh = frame.getRegionHeight();
            float aw = getWidth();
            float ah = getHeight();
            float scale = Math.min(aw / fw, ah / fh);
            float dw = fw * scale;
            float dh = fh * scale;
            float dx = getX() + (aw - dw) * 0.5f;
            float dy = getY() + (ah - dh) * 0.5f;
            batch.draw(frame, dx, dy, dw, dh);
        }
    }

    private static class MarkerActor extends Actor {
        private final Texture tabHovered;
        private final Texture tabNormal;
        private final Texture iconHovered;
        private final Texture iconNormal;
        private boolean hovering = false;
        boolean keyboardActive = false;

        MarkerActor(Texture tabHovered, Texture tabNormal, Texture iconHovered, Texture iconNormal) {
            this.tabHovered = tabHovered;
            this.tabNormal = tabNormal;
            this.iconHovered = iconHovered;
            this.iconNormal = iconNormal;
            setTouchable(Touchable.enabled);
            addListener(new InputListener() {
                @Override public boolean mouseMoved(InputEvent e, float x, float y) {
                    hovering = true; return true;
                }
                @Override public void exit(InputEvent e, float x, float y, int p, Actor a) {
                    hovering = false;
                }
            });
        }

        boolean isActive() { return hovering || keyboardActive; }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            Texture bg = isActive() ? tabHovered : tabNormal;
            Texture ic = isActive() ? iconHovered : iconNormal;
            if (bg != null)
                batch.draw(bg, getX(), getY(), getWidth(), getHeight());
            if (ic != null) {
                float iconSize = 9f;
                float ix = getX() + (getWidth() - iconSize) * 0.5f - 2f;
                float iy = getY() + (getHeight() - iconSize) * 0.5f;
                batch.draw(ic, ix, iy, iconSize, iconSize);
            }
        }
    }

    private static class ItemSlot extends Stack {
        final Image bg;
        final Image icon;

        ItemSlot(Drawable bgDrawable) {
            this.bg = new Image(bgDrawable);
            this.icon = new Image();
            this.icon.setVisible(false);
            add(bg);
            add(icon);
        }

        void setItemIcon(Drawable iconDrawable) {
            if (iconDrawable != null) {
                icon.setDrawable(iconDrawable);
                icon.setVisible(true);
            } else {
                icon.setVisible(false);
            }
        }
    }

    private void addMarkers() {
        float markerW = 18f;
        float markerH = 10f;
        float[] xPositions = {18f, 37f, 56f};
        float topY = WINDOW_H - 3.5f;
        String[] iconHoveredPaths = {
            "ui/inventory/iconsitemsbag.png",
            "ui/inventory/iconsitemsbook.png",
            "ui/inventory/iconsitemssword.png"
        };
        String[] iconNormalPaths = {
            "ui/inventory/iconsitemsbag2.png",
            "ui/inventory/iconsitemsbook2.png",
            "ui/inventory/iconsitemssword2.png"
        };
        Texture tabHovTex = null, tabNormTex = null;
        try { tabHovTex = new Texture(Gdx.files.internal("ui/inventory/tabshorizontally tileable0.png")); ownedTextures.add(tabHovTex); } catch (Exception ignored) {}
        try { tabNormTex = new Texture(Gdx.files.internal("ui/inventory/tabshorizontally tileable1.png")); ownedTextures.add(tabNormTex); } catch (Exception ignored) {}
        markerActors = new MarkerActor[3];
        for (int i = 0; i < 3; i++) {
            Texture iconHov = null, iconNorm = null;
            try { iconHov = new Texture(Gdx.files.internal(iconHoveredPaths[i])); ownedTextures.add(iconHov); } catch (Exception ignored) {}
            try { iconNorm = new Texture(Gdx.files.internal(iconNormalPaths[i])); ownedTextures.add(iconNorm); } catch (Exception ignored) {}
            final int tabIdx = i;
            MarkerActor marker = new MarkerActor(tabHovTex, tabNormTex, iconHov, iconNorm);
            marker.setSize(markerW, markerH);
            marker.setPosition(xPositions[i], topY);
            marker.addListener(new InputListener() {
                @Override public boolean touchDown(InputEvent e, float x, float y, int p, int b) {
                    viewModel.setTab(tabIdx); return true;
                }
            });
            markerActors[i] = marker;
            addActor(marker);
        }
    }

    public void setCloseCallback(Runnable callback) {
        this.closeCallback = callback;
    }

    public void setSkillSelectedCallback(java.util.function.Consumer<Integer> callback) {
        this.skillSelectedCallback = callback;
    }

    public void setSkillLockSupplier(java.util.function.BooleanSupplier supplier) {
        this.skillLockSupplier = supplier;
    }

    public void setEatCallback(java.util.function.Consumer<com.github.MichalKC.manylands.component.Item> callback) {
        this.eatCallback = callback;
    }

    public void showInventory() {
        if (storageMode) {
            buildStandardLayout();
        }
        hideReadingOverlay();
        equipSelectionActive = false;
        equipSelectedSlot = 0;
        selectedSkillSlot = 0;
        viewModel.resetHeld();
        viewModel.setTab(InventoryViewModel.TAB_INVENTORY);
        viewModel.selectSlot(0);
        setVisible(true);
        toFront();
        positionCenter();
        invalidateHierarchy();
        validate();
        refreshSelectionFrame();
        refreshItemIcons();
    }

    public void showStorage(Storage storage) {
        buildStorageLayout(storage);
        hideReadingOverlay();
        equipSelectionActive = false;
        equipSelectedSlot = 0;
        viewModel.resetHeld();
        viewModel.setTab(InventoryViewModel.TAB_INVENTORY);
        viewModel.selectSlot(0);
        setVisible(true);
        toFront();
        positionStorage();
        invalidateHierarchy();
        validate();
        refreshSelectionFrame();
        refreshItemIcons();
    }

    public void hideInventory() {
        setVisible(false);
    }

    private void positionCenter() {
        Stage st = getStage();
        float sw = st != null ? st.getWidth() : 320f;
        float sh = st != null ? st.getHeight() : 180f;
        setPosition((sw - WINDOW_W) * 0.5f, (sh - WINDOW_H) * 0.5f);
    }

    private void positionStorage() {
        Stage st = getStage();
        float sw = st != null ? st.getWidth() : 320f;
        float sh = st != null ? st.getHeight() : 180f;
        setPosition((sw - STORAGE_WINDOW_W) * 0.5f, (sh - STORAGE_WINDOW_H) * 0.5f);
    }

    private Drawable safeDrawable(String path) {
        try {
            Texture tex = new Texture(Gdx.files.internal(path));
            ownedTextures.add(tex);
            return new TextureRegionDrawable(new TextureRegion(tex));
        } catch (Exception e) {
            return null;
        }
    }

    private Image safeImage(String path) {
        Drawable d = safeDrawable(path);
        return d != null ? new Image(d) : new Image();
    }

    private Drawable safeNinePatch(String path, int left, int right, int top, int bottom) {
        try {
            Texture tex = new Texture(Gdx.files.internal(path));
            ownedTextures.add(tex);
            NinePatch np = new NinePatch(tex, left, right, top, bottom);
            return new NinePatchDrawable(np);
        } catch (Exception e) {
            return null;
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
            np.scale(0.4f, 0.4f);
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

    private Drawable safeSkinDrawable(String name) {
        try {
            return skin.getDrawable(name);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void setupUI() {
        ownedTextures = new Array<>();
        buildStandardLayout();
    }

    private void buildStandardLayout() {
        storageMode = false;
        storageSelectionActive = false;
        inventorySlots = new Array<>();
        storageSlots = new Array<>();
        tabPanels = new Table[3];
        tabButtons = new Image[3];
        selectionFrame = null;
        storageSelectionFrame = null;
        itemNameLabel = null;
        equipSlots = new Array<>();
        equipSelectionFrame = null;
        equipSelectionActive = false;
        equipSelectedSlot = 0;

        clearChildren();
        setSize(WINDOW_W, WINDOW_H);
        align(Align.top);
        pad(4f);

        Drawable bg = safeNinePatch9("ui/inventory/inventoryBackground.9.png");
        if (bg == null) bg = safeDrawable("ui/inventory/inventoryBackground.png");
        if (bg == null) bg = safeSkinDrawable("frame");
        if (bg != null) setBackground(bg);

        add(buildTabBar()).fillX().height(14f).padBottom(2f).row();

        Stack contentStack = new Stack();
        tabPanels[0] = buildInventoryPanel();
        tabPanels[1] = buildQuestsPanel();
        tabPanels[2] = buildSkillsPanel();
        contentStack.add(tabPanels[0]);
        contentStack.add(tabPanels[1]);
        contentStack.add(tabPanels[2]);
        add(contentStack).expand().fill();

        positionCenter();
        addMarkers();
        showTab(InventoryViewModel.TAB_INVENTORY);
    }

    private void buildStorageLayout(Storage storage) {
        storageMode = true;
        storageSelectionActive = false;
        storageSelectedSlot = 0;
        storageRows = Math.max(1, storage.getRows());
        storageCols = Math.max(1, storage.getCols());
        inventorySlots = new Array<>();
        storageSlots = new Array<>();
        tabPanels = null;
        tabButtons = null;
        selectionFrame = null;
        storageSelectionFrame = null;
        charActorContainer = null;
        itemNameLabel = null;

        clearChildren();
        setSize(STORAGE_WINDOW_W, STORAGE_WINDOW_H);
        align(Align.center);
        pad(5f);

        Drawable bg = safeNinePatch9("ui/inventory/storageBackground.9.png");
        if (bg == null) bg = safeNinePatch9("ui/inventory/inventoryBackground.9.png");
        if (bg == null) bg = safeDrawable("ui/inventory/inventoryBackground.png");
        if (bg == null) bg = safeSkinDrawable("frame");
        if (bg != null) setBackground(bg);

        Table playerPanel = buildStorageSide("PLAYER", InventoryViewModel.GRID_ROWS, InventoryViewModel.GRID_COLS, true);
        Table storagePanel = buildStorageSide(storage.getTitle(), storageRows, storageCols, false);
        add(playerPanel).width(88f).height(104f).padRight(6f).center();
        add(storagePanel).width(88f).height(104f).center();
    }

    private Table buildStorageSide(String titleText, int rows, int cols, boolean playerInventory) {
        Table panel = new Table();
        panel.align(Align.top);
        panel.pad(5f);
        Drawable panelBg = safeNinePatch9("ui/inventory/inventoryInsideBack.9.png");
        if (panelBg == null) panelBg = safeNinePatch9("ui/inventory/9SlicedObjectswindowsdetails0.png");
        if (panelBg != null) panel.setBackground(panelBg);

        try {
            Label title = new Label(titleText, skin, "small");
            title.setFontScale(0.55f);
            panel.add(title).padBottom(4f).row();
        } catch (Exception ignored) {
            panel.add().height(10f).row();
        }

        Table grid = new Table();
        int safeRows = Math.max(1, rows);
        int safeCols = Math.max(1, cols);
        for (int row = 0; row < safeRows; row++) {
            for (int col = 0; col < safeCols; col++) {
                final int idx = row * safeCols + col;
                ItemSlot slot = new ItemSlot(safeDrawable(playerInventory
                    ? "ui/inventory/9SlicedObjectsslot10.png"
                    : "ui/inventory/9SlicedObjectsslot11.png"));
                slot.setTouchable(Touchable.enabled);
                if (playerInventory) {
                    onClick(slot, () -> {
                        if (storageMode) {
                            equipSelectionActive = false;
                            storageSelectionActive = false;
                            viewModel.selectSlot(idx);
                            viewModel.selectOrMoveItem(idx, false);
                        } else {
                            equipSelectionActive = false;
                            viewModel.selectSlot(idx);
                        }
                        refreshSelectionFrame();
                    });
                    inventorySlots.add(slot);
                } else {
                    onClick(slot, () -> {
                        selectStorageSlot(idx);
                        viewModel.selectOrMoveItem(idx, true);
                        refreshSelectionFrame();
                    });
                    storageSlots.add(slot);
                }
                grid.add(slot).size(15f, 15f).pad(1f);
            }
            grid.row();
        }
        panel.add(grid).expand().top();

        Drawable selDr = safeDrawable("ui/inventory/inputs12.png");
        if (playerInventory && selDr != null) {
            selectionFrame = new Image(selDr);
            selectionFrame.setTouchable(Touchable.disabled);
            selectionFrame.setVisible(false);
        } else if (!playerInventory) {
            Drawable storageSelDr = safeDrawable("ui/inventory/inputs11.png");
            if (storageSelDr != null) {
                storageSelectionFrame = new Image(storageSelDr);
                storageSelectionFrame.setTouchable(Touchable.disabled);
                storageSelectionFrame.setVisible(false);
            }
        }
        return panel;
    }

    private void selectStorageSlot(int slot) {
        if (slot < 0 || slot >= storageSlots.size) return;
        storageSelectionActive = true;
        storageSelectedSlot = slot;
        refreshSelectionFrame();
    }

    @Override
    protected void setupPropertyChanges() {
        viewModel.onPropertyChange(InventoryViewModel.PROP_TAB, Integer.class, this::showTab);
        viewModel.onPropertyChange(InventoryViewModel.PROP_SLOT, Integer.class, idx -> refreshSelectionFrame());
        viewModel.onPropertyChange(InventoryViewModel.PROP_PAGE, Integer.class, idx -> refreshItemIcons());
        viewModel.onPropertyChange(InventoryViewModel.PROP_STATS, InventoryViewModel.class, vm -> {
            refreshStats();
            refreshItemIcons();
            activatedSkillSlot = viewModel.getPersistedActivatedSkillSlot();
            applySkillIconColors();
            refreshSkillPreview();
            if (skillSelectedCallback != null) {
                int hudIdx = (activatedSkillSlot >= 0 && activatedSkillSlot < SKILL_HUD_ICONS.length)
                    ? SKILL_HUD_ICONS[activatedSkillSlot]
                    : -1;
                skillSelectedCallback.accept(hudIdx);
            }
        });
    }

    private Table buildTabBar() {
        Table bar = new Table();
        bar.align(Align.left);

        String[] iconPaths = {
            "ui/inventory/iconsitemsbag.png",
            "ui/inventory/iconsitemsbook.png",
            "ui/inventory/iconsitemssword.png"
        };

        bar.add().width(62f);

        try {
            coinLabel = new Label("2.584", skin, "small");
            coinLabel.setFontScale(0.45f);
            coinLabel.setColor(0.7f, 1f, 0.1f, 1f);

            Image coinImg = safeImage("ui/inventory/iconsitemscoin.png");

            Table foreground = new Table();
            foreground.add(coinImg).size(12f, 12f).padRight(2f).padLeft(5f).padBottom(-2f).bottom();
            foreground.add(coinLabel).padBottom(1.8f).bottom();
            foreground.padBottom(1.8f);

            coinWrapper = new Table();
            coinWrapper.align(Align.bottom);
            try {
                Texture coinBackTex = new Texture(Gdx.files.internal("ui/inventory/inventoryCoinBack.9.png"));
                ownedTextures.add(coinBackTex);
                TextureRegion coinBackRegion = new TextureRegion(coinBackTex, 1, 1, coinBackTex.getWidth() - 2, coinBackTex.getHeight() - 2);
                com.badlogic.gdx.scenes.scene2d.ui.Container<Image> coinBackContainer = new com.badlogic.gdx.scenes.scene2d.ui.Container<>(new Image(new TextureRegionDrawable(coinBackRegion)));
                coinBackContainer.size(26f, 9f);
                coinBackContainer.padTop(8f);
                coinWrapper.add(coinBackContainer).padLeft(10f).bottom();
            } catch (Exception ignored) {}
            coinWrapper.add(foreground).padLeft(-50f).bottom();

            bar.add(coinWrapper).padTop(6f).bottom();
        } catch (Exception e) {
            coinLabel = null;
        }

        bar.add().expandX();

        return bar;
    }

    private Table buildInventoryPanel() {
        Table panel = new Table();
        panel.align(Align.top);

        Table left = new Table();
        left.align(Align.top);
        Drawable leftBg = safeNinePatch9("ui/inventory/inventoryCharacterFrame.9.png");
        if (leftBg == null) leftBg = safeNinePatch9("ui/inventory/inventoryInsideBack.9.png");
        if (leftBg != null) left.setBackground(leftBg);
        left.pad(2f);

        charActorContainer = new Stack();
        charActorContainer.add(buildWarriorActor());
        left.add(charActorContainer).size(66f, 56f).padBottom(-12f).row();

        Table equip = new Table();
        Drawable equipSlotBg = safeDrawable("ui/inventory/charequipslots8.png");
        for (int i = 0; i < 6; i++) {
            final int equipIdx = i;
            Drawable slotBg = safeDrawable("ui/inventory/charequipslots" + (i + 8) + ".png");
            if (slotBg == null) slotBg = equipSlotBg;
            ItemSlot slot = new ItemSlot(slotBg != null ? slotBg : safeSkinDrawable("frame"));
            slot.setTouchable(Touchable.enabled);
            onClick(slot, () -> {
                equipSelectionActive = true;
                equipSelectedSlot = equipIdx;
                viewModel.selectOrMoveEquip(equipIdx);
                refreshSelectionFrame();
                refreshItemIcons();
            });
            equipSlots.add(slot);
            equip.add(slot).size(13f, 13f).pad(0.5f);
            if (i == 2) equip.row();
        }
        Drawable equipSelDr = safeDrawable("ui/inventory/inputs10.png");
        if (equipSelDr != null) {
            equipSelectionFrame = new Image(equipSelDr);
            equipSelectionFrame.setTouchable(Touchable.disabled);
            equipSelectionFrame.setVisible(false);
        }
        left.add(equip).padBottom(1f).row();

        Table stats = new Table();
        statLabels = new Label[4];
        for (int i = 0; i < 4; i++) {
            Image icon = safeImage("ui/inventory/charstatsslots" + (i + 4) + ".png");
            stats.add(icon).size(9f, 9f).padRight(1f);
            try {
                statLabels[i] = new Label("0", skin, "small");
                statLabels[i].setFontScale(0.45f);
                stats.add(statLabels[i]).padRight(3f);
            } catch (Exception ignored) {}
            if (i == 1) stats.row();
        }
        left.add(stats).padBottom(4f).row();
        left.add().expandY();

        panel.add(left).width(58f).top().padRight(4f).padTop(-6f);

        Table right = new Table();
        right.align(Align.top);

        try {
            Label title = new Label("INVENTORY", skin, "small");
            title.setFontScale(0.6f);
            right.add(title).padBottom(2f).row();
        } catch (Exception ignored) {
            right.add().padBottom(2f).row();
        }

        Table grid = new Table();
        for (int row = 0; row < InventoryViewModel.GRID_ROWS; row++) {
            for (int col = 0; col < InventoryViewModel.GRID_COLS; col++) {
                final int idx = row * InventoryViewModel.GRID_COLS + col;
                ItemSlot slot = new ItemSlot(safeDrawable("ui/inventory/9SlicedObjectsslot10.png"));
                slot.setTouchable(Touchable.enabled);
                onClick(slot, () -> {
                    equipSelectionActive = false;
                    viewModel.selectSlot(idx);
                    viewModel.selectOrMoveItem(idx, false);
                    refreshSelectionFrame();
                    refreshItemIcons();
                });
                inventorySlots.add(slot);
                grid.add(slot).size(15f, 15f).pad(1f);
            }
            grid.row();
        }
        right.add(grid).row();

        try {
            itemNameLabel = new Label("", skin, "small");
            itemNameLabel.setFontScale(0.45f);
            right.add(itemNameLabel).padTop(2f).row();
        } catch (Exception ignored) {
            itemNameLabel = null;
        }

        Drawable selDr = safeDrawable("ui/inventory/inputs12.png");
        if (selDr != null) {
            selectionFrame = new Image(selDr);
            selectionFrame.setTouchable(Touchable.disabled);
            selectionFrame.setVisible(false);
        }

        panel.add(right).width(86f).top().padTop(4f);
        return panel;
    }

    private Table buildQuestsPanel() {
        Table panel = new Table();
        panel.align(Align.topLeft);
        panel.pad(2f).padTop(-14f);

        try {
            Label header = new Label("QUEST SELECTION", skin, "small");
            header.setFontScale(0.5f);
            Table headerWrap = new Table();
            Drawable titleFrame = safeNinePatch9("ui/inventory/inventoryTitleFrame.9.png");
            if (titleFrame != null) headerWrap.setBackground(titleFrame);
            headerWrap.add(header).padLeft(4f).padRight(4f).padTop(1f).padBottom(1f);
            panel.add(headerWrap).center().padBottom(3f).colspan(2).row();
        } catch (Exception ignored) {}

        Drawable textBg = safeNinePatch9("ui/inventory/inventoryQuestText.9.png");
        if (textBg == null) textBg = safeNinePatch9("ui/inventory/inventoryInsideBack.9.png");
        Table textArea = new Table();
        if (textBg != null) textArea.setBackground(textBg);
        textArea.pad(4f).padTop(5f);
        try {
            Label desc = new Label(
                "Quests Unavailable",
                skin, "small"
            );
            desc.setFontScale(0.55f);
            desc.setWrap(true);
            textArea.add(desc).width(76f).top();
        } catch (Exception ignored) {}
        panel.add(textArea).size(88f, 80f).top();

        Drawable listBg = safeNinePatch9("ui/inventory/inventoryQuestList.9.png");
        if (listBg == null) listBg = safeNinePatch9("ui/inventory/inventoryInsideBack.9.png");
        Table list = new Table();
        if (listBg != null) list.setBackground(listBg);
        list.padLeft(4f).padRight(2f).padTop(5f).padBottom(4f);
        list.align(Align.topLeft);
        // 1 pergamin na górze, lekko w prawo
        Image firstScroll = safeImage("ui/inventory/iconsitemspaperroll.png");
        list.add(firstScroll).size(14f, 14f).padBottom(2f).padLeft(4f).left().row();
        // Sloty 9-patch w ScrollPane - 5 przycisków togglowalnych
        final Drawable slotBgUnpressed = safeNinePatch9("ui/inventory/inventoryQuestButtonListNotPressed.9.png");
        final Drawable slotBgPressed = safeNinePatch9("ui/inventory/inventoryQuestButtonListPressed.9.png");
        Table buttonsTable = new Table();
        buttonsTable.align(Align.topLeft);
        questButtons = new Image[5];
        activeQuestButton = -1;
        for (int i = 0; i < 5; i++) {
            final int btnIdx = i;
            Image btn = new Image(slotBgUnpressed != null ? slotBgUnpressed : safeSkinDrawable("frame"));
            btn.setTouchable(Touchable.enabled);
            onClick(btn, () -> toggleQuestButton(btnIdx, slotBgUnpressed, slotBgPressed));
            questButtons[i] = btn;
            buttonsTable.add(btn).size(48f, 14f).padBottom(-2f).left().row();
        }
        final ScrollPane questScroll = new ScrollPane(buttonsTable);
        questScroll.setScrollingDisabled(true, false);
        questScroll.setFadeScrollBars(false);
        questScroll.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (questScroll.getStage() != null) {
                    questScroll.getStage().setScrollFocus(questScroll);
                }
            }
            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (questScroll.getStage() != null && toActor != questScroll) {
                    questScroll.getStage().setScrollFocus(null);
                }
            }
        });
        list.add(questScroll).width(50f).height(56f).padTop(-3f).left();
        panel.add(list).size(56f, 80f).top();
        return panel;
    }

    private void toggleQuestButton(int idx, Drawable unpressed, Drawable pressed) {
        if (questButtons == null || idx < 0 || idx >= questButtons.length) return;
        if (activeQuestButton == idx) {
            questButtons[idx].setDrawable(unpressed);
            activeQuestButton = -1;
        } else {
            if (activeQuestButton >= 0 && activeQuestButton < questButtons.length) {
                questButtons[activeQuestButton].setDrawable(unpressed);
            }
            questButtons[idx].setDrawable(pressed);
            activeQuestButton = idx;
        }
    }

    private Stack buildSkillSlot(int slotFrameIdx, int iconIdx) {
        Stack slot = new Stack();
        Image frame = safeImage("ui/inventory/skillslotsui" + slotFrameIdx + ".png");
        slot.add(frame);
        if (iconIdx >= 0) {
            Image icon = safeImage("ui/inventory/skillBlocked.png");
            slot.add(icon);
        }
        return slot;
    }

    private void refreshSkillSelection() {
        if (skillSlotActors == null) return;
        for (int i = 0; i < skillSlotActors.size; i++) {
            Stack slot = skillSlotActors.get(i);
            int frameIdx = (i == selectedSkillSlot) ? SKILL_FRAMES_SELECTED[i] : SKILL_FRAMES_NORMAL[i];
            Image frameImg = safeImage("ui/inventory/skillslotsui" + frameIdx + ".png");
            com.badlogic.gdx.scenes.scene2d.Actor first = slot.getChildren().size > 0 ? slot.getChildren().get(0) : null;
            if (first instanceof Image) {
                ((Image) first).setDrawable(frameImg.getDrawable());
            }
        }
        updateSkillDesc();
    }

    private Stack buildEmptySkillSlot() {
        Stack slot = new Stack();
        Image empty = safeImage("ui/inventory/skilliconslothudempty0.png");
        slot.add(empty);
        return slot;
    }

    private Table buildSkillsPanel() {
        Table panel = new Table();
        panel.align(Align.topLeft);
        panel.pad(2f).padTop(-14f).padLeft(8f);

        try {
            Label header = new Label("SKILLS", skin, "small");
            header.setFontScale(0.5f);
            Table headerWrap = new Table();
            Drawable titleFrame = safeNinePatch9("ui/inventory/inventoryTitleFrame.9.png");
            if (titleFrame != null) headerWrap.setBackground(titleFrame);
            headerWrap.add(header).padLeft(4f).padRight(4f).padTop(1f).padBottom(1f);
            panel.add(headerWrap).center().padBottom(3f).colspan(2).row();
        } catch (Exception ignored) {}

        // Tree area - 8 circular slots in 3 rows: 2 centered, 3, 3
        Table treeArea = new Table();
        Drawable insideBg = safeNinePatch9("ui/inventory/inventorySkills.9.png");
        if (insideBg == null) insideBg = safeNinePatch9("ui/inventory/inventoryInsideBack.9.png");
        treeArea.pad(2f).padTop(2f);
        treeArea.align(Align.top);

        // 8 circular skill slots with specific frames in order:
        // skillslotsui1, skillslotsui10, skillslotsui2, skillslotsui13,
        // skillslotsui12, skillslotsui3, skillslotsui15, skillslotsui14
        float ss = 14f; // slot size (slightly smaller)
        float spH = 3f;  // horizontal spacing
        float spV = 1f;  // vertical spacing between rows (reduced)
        float lowerOffset = 10f; // extra offset for slots 13 and 15 (lowered more)
        int[] slotFrames = {1, 10, 2, 13, 12, 3, 15, 14};
        int[] slotIcons = SKILL_SLOT_ICONS;

        skillSlotActors = new Array<>();
        for (int i = 0; i < 8; i++) {
            final int slotIdx = i;
            Stack s = buildSkillSlot(slotFrames[i], slotIcons[i]);
            s.setTouchable(Touchable.enabled);
            s.addListener(new InputListener() {
                @Override public boolean touchDown(InputEvent e, float x, float y, int p, int b) {
                    if (viewModel.getCurrentTab() == InventoryViewModel.TAB_SKILLS) {
                        selectedSkillSlot = slotIdx;
                        refreshSkillSelection();
                        activateSkillSlot(slotIdx);
                    }
                    return true;
                }
            });
            skillSlotActors.add(s);
        }

        // Row 0: 2 slots centered (slot 0, 1)
        // Use a nested table to properly center them
        Table topRow = new Table();
        topRow.add(skillSlotActors.get(0)).size(ss,ss).pad(spH, spH, spV, spH);
        topRow.add(skillSlotActors.get(1)).size(ss,ss).pad(spH, spH, spV, spH);
        treeArea.add(topRow).colspan(3).center().row();

        // Row 1: 3 slots (slot 2, 3, 4) - slot 3 (frame 13) lowered
        treeArea.add(skillSlotActors.get(2)).size(ss,ss).pad(spH, spH, spV, spH);
        treeArea.add(skillSlotActors.get(3)).size(ss,ss).pad(spH + lowerOffset, spH, spV, spH);
        treeArea.add(skillSlotActors.get(4)).size(ss,ss).pad(spH, spH, spV, spH);
        treeArea.row();

        // Row 2: 3 slots (slot 5, 6, 7) - slot 6 (frame 15) lowered
        treeArea.add(skillSlotActors.get(5)).size(ss,ss).pad(spH, spH, spV, spH);
        treeArea.add(skillSlotActors.get(6)).size(ss,ss).pad(spH + lowerOffset, spH, spV, spH);
        treeArea.add(skillSlotActors.get(7)).size(ss,ss).pad(spH, spH, spV, spH);

        Drawable connDr = safeNinePatch9("ui/inventory/skillsConnection.9.png");
        ConnectionsActor connectionsActor = new ConnectionsActor(connDr, viewModel::isSkillUnlocked);
        Stack treeStack = new Stack();
        if (insideBg != null) treeStack.add(new Image(insideBg));
        treeStack.add(connectionsActor);
        treeStack.add(treeArea);
        panel.add(treeStack).size(85f, 82f).top().padRight(2f);

        // Right column: single panel
        Table descText = new Table();
        Drawable descBg = safeNinePatch9("ui/inventory/inventorySkillsText.9.png");
        if (descBg == null) descBg = safeNinePatch9("ui/inventory/inventoryInsideBack.9.png");
        if (descBg != null) descText.setBackground(descBg);
        descText.pad(3f).padTop(5f);
        descText.align(Align.top);

        skillPreviewSlot = buildEmptySkillSlot();
        descText.add(skillPreviewSlot).size(16f, 16f).padBottom(2f).row();

        try {
            skillDescLabel = new Label(
                "Select a skill.",
                skin, "small"
            );
            skillDescLabel.setFontScale(0.35f);
            skillDescLabel.setWrap(true);
            descText.add(skillDescLabel).width(36f).top();
        } catch (Exception ignored) { skillDescLabel = null; }

        panel.add(descText).size(46f, 82f).top();
        activatedSkillSlot = viewModel.getPersistedActivatedSkillSlot();
        applySkillIconColors();
        refreshSkillPreview();
        updateSkillDesc();
        if (skillSelectedCallback != null) {
            skillSelectedCallback.accept(
                activatedSkillSlot >= 0 ? SKILL_HUD_ICONS[activatedSkillSlot] : -1
            );
        }
        return panel;
    }

    private void activateSkillSlot(int slot) {
        if (skillLockSupplier != null && skillLockSupplier.getAsBoolean()) return;
        if (!viewModel.isSkillUnlocked(slot)) return;
        activatedSkillSlot = (slot == activatedSkillSlot) ? -1 : slot;
        viewModel.setPersistedActivatedSkillSlot(activatedSkillSlot);
        applySkillIconColors();
        refreshSkillPreview();
        if (skillSelectedCallback != null) {
            skillSelectedCallback.accept(
                activatedSkillSlot >= 0 ? SKILL_HUD_ICONS[activatedSkillSlot] : -1
            );
        }
    }

    private void applySkillIconColors() {
        if (skillSlotActors == null) return;
        for (int i = 0; i < skillSlotActors.size; i++) {
            Stack slot = skillSlotActors.get(i);
            if (slot.getChildren().size > 1) {
                com.badlogic.gdx.scenes.scene2d.Actor iconActor = slot.getChildren().get(1);
                if (iconActor instanceof Image) {
                    Color c;
                    if (!viewModel.isSkillUnlocked(i)) {
                        c = Color.WHITE;
                    } else if (i == activatedSkillSlot) {
                        c = Color.WHITE;
                    } else {
                        c = new Color(0.35f, 0.35f, 0.35f, 1f);
                    }
                    ((Image) iconActor).setColor(c);
                }
            }
        }
    }

    private void refreshSkillIcons() {
        if (skillSlotActors == null) return;
        for (int i = 0; i < skillSlotActors.size; i++) {
            Stack slot = skillSlotActors.get(i);
            if (slot.getChildren().size > 1) {
                com.badlogic.gdx.scenes.scene2d.Actor iconActor = slot.getChildren().get(1);
                if (iconActor instanceof Image) {
                    String path = viewModel.isSkillUnlocked(i)
                        ? "ui/inventory/skilliconcropped" + SKILL_SLOT_ICONS[i] + ".png"
                        : "ui/inventory/skillBlocked.png";
                    ((Image) iconActor).setDrawable(safeDrawable(path));
                }
            }
        }
    }

    private void updateSkillDesc() {
        if (skillDescLabel == null) return;
        int idx = selectedSkillSlot;
        if (idx < 0 || idx >= 8) {
            skillDescLabel.setText("Select a skill.");
            return;
        }
        if (!viewModel.isSkillUnlocked(idx)) {
            skillDescLabel.setText("Locked");
            return;
        }
        skillDescLabel.setText(getSkillDescription(idx));
    }

    private String getSkillDescription(int idx) {
        switch (idx) {
            case 0: return "Power Slash — Imbue your primary strike to deal +30% damage.";
            case 1: return "Twin Fang — Empower your secondary technique: +30% damage.";
            case 2: return "Quickening — Primary attack speed surges by 40%.";
            case 3: return "Veil of Shadows — Slip unseen; the veil breaks if you act or take harm.";
            case 4: return "Overclock — Secondary attack executes 40% faster.";
            case 5: return "Battle Trance — Your Super is always ready; the gauge thrums with power.";
            case 6: return "Medusa's Gaze — Turn foes to stone. Striking one frees only the struck.";
            case 7: return "Bloodlust — All attacks deal +25% damage.";
            default: return "Skill";
        }
    }

    private void refreshSkillPreview() {
        if (skillPreviewSlot == null) return;
        skillPreviewSlot.clearChildren();
        skillPreviewSlot.add(safeImage("ui/inventory/skilliconslothudempty0.png"));
        if (activatedSkillSlot >= 0 && activatedSkillSlot < SKILL_HUD_ICONS.length) {
            int hudIdx = SKILL_HUD_ICONS[activatedSkillSlot];
            Image hudIcon = safeImage("ui/inventory/skilliconslothud" + hudIdx + ".png");
            com.badlogic.gdx.scenes.scene2d.ui.Container<Image> iconWrap =
                new com.badlogic.gdx.scenes.scene2d.ui.Container<>(hudIcon);
            iconWrap.pad(2f);
            iconWrap.fill();
            skillPreviewSlot.add(iconWrap);
        }
    }

    private void showTab(int tab) {
        hideReadingOverlay();
        if (tabPanels == null) return;
        for (int i = 0; i < tabPanels.length; i++) {
            if (tabPanels[i] != null) tabPanels[i].setVisible(i == tab);
        }
        if (markerActors != null) {
            for (int i = 0; i < markerActors.length; i++) {
                if (markerActors[i] != null) markerActors[i].keyboardActive = (i == tab);
            }
        }
        if (coinWrapper != null) {
            coinWrapper.setVisible(tab == InventoryViewModel.TAB_INVENTORY);
        }
        refreshSelectionFrame();
        if (tab == InventoryViewModel.TAB_SKILLS) {
            refreshSkillIcons();
            refreshSkillSelection();
            applySkillIconColors();
            refreshSkillPreview();
        }
    }

    private void refreshStats() {
        if (statLabels == null) return;
        int[] vals = {
            viewModel.getStatLife(),
            viewModel.getStatAttack(),
            viewModel.getStatDefense(),
            viewModel.getStatSpeed()
        };
        for (int i = 0; i < statLabels.length; i++) {
            if (statLabels[i] != null) statLabels[i].setText(String.valueOf(vals[i]));
        }
        if (coinLabel != null) coinLabel.setText(String.valueOf(viewModel.getCoins()));
    }

    private void refreshItemName() {
        if (itemNameLabel == null) return;
        boolean onInventoryTab = viewModel.getCurrentTab() == InventoryViewModel.TAB_INVENTORY
            && !storageSelectionActive && !equipSelectionActive;
        if (readingActive) onInventoryTab = false;
        int heldSlot = viewModel.getHeldSlot();
        boolean heldFromMainInv = heldSlot >= 0
            && !viewModel.isHeldInStorage() && !viewModel.isHeldInEquip();

        if (!onInventoryTab) {
            itemNameLabel.setText("");
            if (itemTooltip != null) itemTooltip.setVisible(false);
            return;
        }

        com.github.MichalKC.manylands.component.Inventory inv = viewModel.getPlayerInventory();
        com.github.MichalKC.manylands.component.Item[] slots = inv != null ? inv.getSlots() : null;
        int idx = heldFromMainInv
            ? heldSlot
            : viewModel.getSelectedSlot() + viewModel.getCurrentPage() * 16;
        com.github.MichalKC.manylands.component.Item item =
            (slots != null && idx >= 0 && idx < slots.length) ? slots[idx] : null;
        itemNameLabel.setText(item != null ? item.getName() : "");

        if (heldFromMainInv) {
            refreshItemTooltip(item, heldSlot);
        } else if (itemTooltip != null) {
            itemTooltip.setVisible(false);
        }
    }

    private void refreshItemTooltip(com.github.MichalKC.manylands.component.Item item, int slotIdx) {
        if (item == null || storageMode) {
            if (itemTooltip != null) itemTooltip.setVisible(false);
            return;
        }
        boolean hasInfo = (item.getWear() != null && !item.getWear().isEmpty())
            || item.isEat() || item.isRead()
            || item.getPlusHP() != 0 || item.getPlusAttack1() != 0 || item.getPlusAttack2() != 0 || item.getPlusAttack3() != 0
            || item.getPlusDefense() != 0 || item.getPlusSpeed() != 0;
        if (!hasInfo) {
            if (itemTooltip != null) itemTooltip.setVisible(false);
            return;
        }
        // Build tooltip text
        StringBuilder sb = new StringBuilder();
        if (item.getWear() != null && !item.getWear().isEmpty()) {
            String sn = item.getWear();
            switch (sn) {
                case "head": sn = "Head"; break;
                case "chest": sn = "Chest"; break;
                case "gloves": sn = "Gloves"; break;
                case "weapon": sn = "Weapon"; break;
                case "specialObject": sn = "Special"; break;
                case "boots": sn = "Boots"; break;
            }
            sb.append("Equip: ").append(sn).append("\n");
        }
        if (item.isEat()) sb.append("Eat: HP +").append(item.getPlusHP()).append("\n");
        else if (item.getPlusHP() != 0) sb.append("HP +").append(item.getPlusHP()).append("\n");
        if (item.getPlusAttack1() != 0) sb.append("ATK1 +").append(item.getPlusAttack1()).append("\n");
        if (item.getPlusAttack2() != 0) sb.append("ATK2 +").append(item.getPlusAttack2()).append("\n");
        if (item.getPlusAttack3() != 0) sb.append("ATK3 +").append(item.getPlusAttack3()).append("\n");
        if (item.getPlusDefense() != 0) sb.append("DEF +").append(item.getPlusDefense()).append("\n");
        if (item.getPlusSpeed() != 0) sb.append("SPD +").append(item.getPlusSpeed()).append("\n");
        if (item.isRead()) sb.append("You can read\n");

        // Create tooltip if needed
        if (itemTooltip == null) {
            itemTooltip = new Table();
            itemTooltip.setTouchable(Touchable.disabled);
            Drawable ttBg = safeNinePatch9("ui/inventory/inventorySkillsText.9.png");
            if (ttBg == null) ttBg = safeNinePatch9("ui/inventory/inventoryInsideBack.9.png");
            if (ttBg != null) itemTooltip.setBackground(ttBg);
            itemTooltip.pad(3f);
            try {
                itemTooltipLabel = new Label("", skin, "small");
                itemTooltipLabel.setFontScale(0.35f);
                itemTooltipLabel.setWrap(true);
                itemTooltipLabel.setColor(Color.WHITE);
                itemTooltip.add(itemTooltipLabel).width(50f).top();
            } catch (Exception ignored) {}
        }
        if (itemTooltipLabel != null) itemTooltipLabel.setText(sb.toString().trim());
        itemTooltip.pack();

        // Position near the given slot
        if (slotIdx >= 0 && inventorySlots != null && slotIdx < inventorySlots.size) {
            com.badlogic.gdx.scenes.scene2d.Actor slot = inventorySlots.get(slotIdx);
            Vector2 pos = new Vector2(0, 0);
            slot.localToActorCoordinates(this, pos);
            if (itemTooltip.getParent() != this) addActor(itemTooltip);
            itemTooltip.setPosition(pos.x + slot.getWidth() + 2f, pos.y - itemTooltip.getPrefHeight() + slot.getHeight());
            itemTooltip.setVisible(true);
            itemTooltip.toFront();
        }
    }

    private void refreshSelectionFrame() {
        if (selectionFrame == null || inventorySlots == null) return;
        if (viewModel.getCurrentTab() != InventoryViewModel.TAB_INVENTORY || !isVisible() || storageSelectionActive || equipSelectionActive) {
            selectionFrame.setVisible(false);
            if (itemTooltip != null) itemTooltip.setVisible(false);
        } else {
            int idx = viewModel.getSelectedSlot();
            if (idx < 0 || idx >= inventorySlots.size) {
                selectionFrame.setVisible(false);
            } else {
                Actor slot = inventorySlots.get(idx);
                com.badlogic.gdx.math.Vector2 v = new com.badlogic.gdx.math.Vector2(0, 0);
                slot.localToActorCoordinates(this, v);
                if (selectionFrame.getParent() != this) {
                    addActor(selectionFrame);
                }
                selectionFrame.setSize(slot.getWidth(), slot.getHeight());
                selectionFrame.setPosition(v.x, v.y);
                selectionFrame.setVisible(true);
                selectionFrame.toFront();
            }
        }

        refreshItemName();
        refreshEquipFrame();

        if (storageSelectionFrame == null) return;
        if (!storageMode || !storageSelectionActive || storageSelectedSlot < 0 || storageSelectedSlot >= storageSlots.size) {
            storageSelectionFrame.setVisible(false);
            return;
        }
        Actor slot = storageSlots.get(storageSelectedSlot);
        com.badlogic.gdx.math.Vector2 v = new com.badlogic.gdx.math.Vector2(0, 0);
        slot.localToActorCoordinates(this, v);
        if (storageSelectionFrame.getParent() != this) {
            addActor(storageSelectionFrame);
        }
        storageSelectionFrame.setSize(slot.getWidth(), slot.getHeight());
        storageSelectionFrame.setPosition(v.x, v.y);
        storageSelectionFrame.setVisible(true);
        storageSelectionFrame.toFront();
    }

    private void refreshEquipFrame() {
        if (equipSelectionFrame == null || equipSlots == null) return;
        if (!equipSelectionActive || equipSelectedSlot < 0 || equipSelectedSlot >= equipSlots.size
                || viewModel.getCurrentTab() != InventoryViewModel.TAB_INVENTORY) {
            equipSelectionFrame.setVisible(false);
            return;
        }
        Actor slot = equipSlots.get(equipSelectedSlot);
        com.badlogic.gdx.math.Vector2 v = new com.badlogic.gdx.math.Vector2(0, 0);
        slot.localToActorCoordinates(this, v);
        if (equipSelectionFrame.getParent() != this) addActor(equipSelectionFrame);
        equipSelectionFrame.setSize(slot.getWidth(), slot.getHeight());
        equipSelectionFrame.setPosition(v.x, v.y);
        equipSelectionFrame.setVisible(true);
        equipSelectionFrame.toFront();
    }

    private void refreshItemIcons() {
        if (inventorySlots == null) return;

        com.github.MichalKC.manylands.component.Inventory playerInv = viewModel.getPlayerInventory();
        com.github.MichalKC.manylands.component.Item[] pSlots = playerInv != null ? playerInv.getSlots() : null;

        int currentPage = viewModel.getCurrentPage();
        for (int i = 0; i < inventorySlots.size; i++) {
            ItemSlot slot = inventorySlots.get(i);
            int itemIndex = storageMode ? i : (currentPage * 16 + i);
            com.github.MichalKC.manylands.component.Item item = (pSlots != null && itemIndex < pSlots.length) ? pSlots[itemIndex] : null;

            if (item != null) {
                slot.setItemIcon(safeDrawable(item.getTexturePath()));
            } else {
                slot.setItemIcon(null);
            }

            int heldSlot = viewModel.getHeldSlot();
            boolean heldInStorage = viewModel.isHeldInStorage();
            boolean heldInEquip = viewModel.isHeldInEquip();
            if (heldSlot == itemIndex && !heldInStorage && !heldInEquip) {
                slot.bg.setColor(0.7f, 0.7f, 0.7f, 1f);
            } else {
                slot.bg.setColor(Color.WHITE);
            }
        }

        if (equipSlots != null) {
            com.github.MichalKC.manylands.component.Item[] eSlots = viewModel.getEquipSlots();
            for (int i = 0; i < equipSlots.size; i++) {
                ItemSlot eslot = equipSlots.get(i);
                com.github.MichalKC.manylands.component.Item eItem = (eSlots != null && i < eSlots.length) ? eSlots[i] : null;
                if (eItem != null) {
                    eslot.setItemIcon(safeDrawable(eItem.getTexturePath()));
                    eslot.bg.setDrawable(safeDrawable("ui/inventory/charequipslots7.png"));
                } else {
                    eslot.setItemIcon(null);
                    Drawable orig = safeDrawable("ui/inventory/charequipslots" + (i + 8) + ".png");
                    if (orig != null) eslot.bg.setDrawable(orig);
                }
                int hs = viewModel.getHeldSlot();
                eslot.bg.setColor(viewModel.isHeldInEquip() && hs == i
                    ? new Color(0.7f, 0.7f, 0.7f, 1f) : Color.WHITE);
            }
        }

        refreshItemName();
        refreshEquipFrame();

        if (storageMode && storageSlots != null) {
            Storage storage = viewModel.getStorage();
            com.github.MichalKC.manylands.component.Item[] sSlots = storage != null ? storage.getSlots() : null;
            for (int i = 0; i < storageSlots.size; i++) {
                ItemSlot slot = storageSlots.get(i);
                com.github.MichalKC.manylands.component.Item item = (sSlots != null && i < sSlots.length) ? sSlots[i] : null;

                if (item != null) {
                    slot.setItemIcon(safeDrawable(item.getTexturePath()));
                } else {
                    slot.setItemIcon(null);
                }

                int heldSlot = viewModel.getHeldSlot();
                boolean heldInStorage = viewModel.isHeldInStorage();
                if (heldSlot == i && heldInStorage) {
                    slot.bg.setColor(0.7f, 0.7f, 0.7f, 1f);
                } else {
                    slot.bg.setColor(Color.WHITE);
                }
            }
        }
    }

    @Override
    public void onLeft() {
        if (!isVisible()) return;
        if (readingActive) return;
        if (viewModel.getCurrentTab() == InventoryViewModel.TAB_SKILLS) {
            selectedSkillSlot = SKILL_NAV_LEFT[selectedSkillSlot];
            refreshSkillSelection();
            return;
        }
        if (equipSelectionActive && viewModel.getCurrentTab() != InventoryViewModel.TAB_INVENTORY) return;
        if (equipSelectionActive) {
            int col = equipSelectedSlot % 3;
            if (col > 0) equipSelectedSlot--;
            // at col 0 - stay, no wrapping back to inventory
            refreshSelectionFrame();
            return;
        }
        if (!storageMode) {
            int col = viewModel.getSelectedSlot() % InventoryViewModel.GRID_COLS;
            if (col == 0) {
                equipSelectionActive = true;
                int row = viewModel.getSelectedSlot() / InventoryViewModel.GRID_COLS;
                equipSelectedSlot = Math.min(row < 2 ? row * 3 + 2 : row * 3 - 6 + 2, equipSlots.size - 1);
                refreshSelectionFrame();
                return;
            }
            viewModel.onLeft();
            return;
        }
        if (storageSelectionActive) {
            int col = storageSelectedSlot % storageCols;
            if (col > 0) {
                storageSelectedSlot--;
            } else {
                storageSelectionActive = false;
            }
            refreshSelectionFrame();
            return;
        }
        viewModel.onLeft();
    }

    @Override
    public void onRight() {
        if (!isVisible()) return;
        if (readingActive) return;
        if (viewModel.getCurrentTab() == InventoryViewModel.TAB_SKILLS) {
            selectedSkillSlot = SKILL_NAV_RIGHT[selectedSkillSlot];
            refreshSkillSelection();
            return;
        }
        if (equipSelectionActive && viewModel.getCurrentTab() != InventoryViewModel.TAB_INVENTORY) return;
        if (equipSelectionActive) {
            int col = equipSelectedSlot % 3;
            if (col < 2 && equipSelectedSlot + 1 < equipSlots.size) {
                equipSelectedSlot++;
            } else {
                equipSelectionActive = false;
                int row = equipSelectedSlot / 3;
                viewModel.selectSlot(row * InventoryViewModel.GRID_COLS);
            }
            refreshSelectionFrame();
            return;
        }
        if (!storageMode) {
            viewModel.onRight();
            return;
        }
        if (storageSelectionActive) {
            int col = storageSelectedSlot % storageCols;
            if (col < storageCols - 1 && storageSelectedSlot + 1 < storageSlots.size) {
                storageSelectedSlot++;
            }
            refreshSelectionFrame();
            return;
        }
        int col = viewModel.getSelectedSlot() % InventoryViewModel.GRID_COLS;
        if (col == InventoryViewModel.GRID_COLS - 1) {
            storageSelectionActive = true;
            int row = viewModel.getSelectedSlot() / InventoryViewModel.GRID_COLS;
            storageSelectedSlot = Math.min(row * storageCols, storageSlots.size - 1);
            refreshSelectionFrame();
        } else {
            viewModel.onRight();
        }
    }

    @Override
    public void onUp() {
        if (!isVisible()) return;
        if (readingActive) { return; }
        if (viewModel.getCurrentTab() == InventoryViewModel.TAB_SKILLS) {
            selectedSkillSlot = SKILL_NAV_UP[selectedSkillSlot];
            refreshSkillSelection();
            return;
        }
        if (equipSelectionActive && viewModel.getCurrentTab() != InventoryViewModel.TAB_INVENTORY) return;
        if (equipSelectionActive) {
            if (equipSelectedSlot >= 3) equipSelectedSlot -= 3;
            refreshSelectionFrame();
            return;
        }
        if (storageMode && storageSelectionActive) {
            if (storageSelectedSlot >= storageCols) {
                storageSelectedSlot -= storageCols;
                refreshSelectionFrame();
            }
            return;
        }
        viewModel.onUp();
    }

    @Override
    public void onDown() {
        if (!isVisible()) return;
        if (readingActive) { return; }
        if (viewModel.getCurrentTab() == InventoryViewModel.TAB_SKILLS) {
            selectedSkillSlot = SKILL_NAV_DOWN[selectedSkillSlot];
            refreshSkillSelection();
            return;
        }
        if (equipSelectionActive && viewModel.getCurrentTab() != InventoryViewModel.TAB_INVENTORY) return;
        if (equipSelectionActive) {
            if (equipSelectedSlot + 3 < equipSlots.size) equipSelectedSlot += 3;
            refreshSelectionFrame();
            return;
        }
        if (storageMode && storageSelectionActive) {
            if (storageSelectedSlot + storageCols < storageSlots.size) {
                storageSelectedSlot += storageCols;
                refreshSelectionFrame();
            }
            return;
        }
        viewModel.onDown();
    }

    @Override
    public void onSelect() {
        if (!isVisible()) return;
        if (readingActive) return;
        if (viewModel.getCurrentTab() == InventoryViewModel.TAB_SKILLS) {
            activateSkillSlot(selectedSkillSlot);
            return;
        }
        if (equipSelectionActive) {
            viewModel.selectOrMoveEquip(equipSelectedSlot);
            return;
        }
        if (!storageMode) {
            int targetSlot = viewModel.getCurrentPage() * 16 + viewModel.getSelectedSlot();
            viewModel.selectOrMoveItem(targetSlot, false);
        } else {
            if (storageSelectionActive) {
                viewModel.selectOrMoveItem(storageSelectedSlot, true);
            } else {
                viewModel.selectOrMoveItem(viewModel.getSelectedSlot(), false);
            }
        }
    }

    @Override
    public void onInteract() {
        if (!isVisible()) return;
        if (readingActive) {
            hideReadingOverlay();
            return;
        }
        if (viewModel.getCurrentTab() != InventoryViewModel.TAB_INVENTORY) return;
        if (storageMode || equipSelectionActive) return;
        int targetSlot = viewModel.getCurrentPage() * 16 + viewModel.getSelectedSlot();
        com.github.MichalKC.manylands.component.Item item = viewModel.getItemAtSlot(targetSlot, false);
        if (item == null) return;
        if (item.isEat()) {
            viewModel.removeItemFromSlot(targetSlot, false);
            refreshItemIcons();
            refreshSelectionFrame();
            if (eatCallback != null) eatCallback.accept(item);
        } else if (item.isRead()) {
            String text = item.getDescription() != null ? item.getDescription() : "";
            showReadingOverlay(text);
        }
    }

    @Override
    public void onCancel() {
        if (readingActive) {
            hideReadingOverlay();
            return;
        }
        if (isVisible() && closeCallback != null) {
            closeCallback.run();
        }
    }

    @Override
    public boolean handle(Event event) {
        if (!isVisible()) return false;
        if (event instanceof UiEvent uiEvent && uiEvent.getCommand() == Command.INVENTORY) {
            if (closeCallback != null) closeCallback.run();
            return true;
        }
        return super.handle(event);
    }

    private void showReadingOverlay(String text) {
        if (readingOverlay == null) {
            readingOverlay = new Table();
            readingOverlay.setTouchable(Touchable.enabled);
            Drawable bg = safeNinePatch9("ui/inventory/inventoryQuestText.9.png");
            if (bg == null) bg = safeNinePatch9("ui/inventory/inventoryInsideBack.9.png");
            if (bg != null) readingOverlay.setBackground(bg);
            readingOverlay.pad(4f).padTop(5f);
            readingOverlay.align(Align.topLeft);
            try {
                readingLabel = new Label("", skin, "small");
                readingLabel.setFontScale(0.5f);
                readingLabel.setWrap(true);
                readingScroll = new ScrollPane(readingLabel);
                readingScroll.setScrollingDisabled(true, false);
                readingScroll.setFadeScrollBars(false);
                readingOverlay.add(readingScroll).width(100f).height(70f).top().left();
            } catch (Exception ignored) {}
        }
        if (readingLabel != null) readingLabel.setText(text.replace('|', '\n'));
        if (readingOverlay.getParent() != this) addActor(readingOverlay);
        float w = 120f, h = 80f;
        readingOverlay.setSize(w, h);
        readingOverlay.setPosition((getWidth() - w) * 0.5f, (getHeight() - h) * 0.5f);
        readingOverlay.setVisible(true);
        readingOverlay.toFront();
        readingActive = true;
        Stage st = getStage();
        if (st != null && readingScroll != null) st.setScrollFocus(readingScroll);
    }

    private void hideReadingOverlay() {
        if (readingOverlay != null) {
            readingOverlay.setVisible(false);
        }
        Stage st = getStage();
        if (st != null && st.getScrollFocus() == readingScroll) st.setScrollFocus(null);
        readingActive = false;
    }

    private Actor buildWarriorActor() {
        if (assetService != null) {
            try {
                TextureAtlas atlas = assetService.get(AtlasAsset.OBJECTS);
                Array<TextureAtlas.AtlasRegion> frames = atlas.findRegions("warrior/idle_right");
                if (!frames.isEmpty()) {
                    Animation<TextureRegion> anim = new Animation<>(1f / 8f, frames, Animation.PlayMode.LOOP);
                    return new AnimatedActor(anim);
                }
            } catch (Exception ignored) {}
        }
        return safeImage("ui/inventory/charequipslots7.png");
    }

    private static class ConnectionsActor extends Actor {
        private static final float THICKNESS = 2f;
        private static final Color DIM_COLOR = new Color(0.4f, 0.4f, 0.4f, 1f);
        // {x1, y1, x2, y2, slotA, slotB} - line lights up when both slots are unlocked
        private static final float[][] CONNECTIONS = {
            {32.5f, 70f, 52.5f, 70f, 0, 1}, // slot 0 → slot 1
            {32.5f, 70f, 22.5f, 52f, 0, 2}, // slot 0 → slot 2
            {32.5f, 70f, 42.5f, 42f, 0, 3}, // slot 0 → slot 3
            {52.5f, 70f, 42.5f, 42f, 1, 3}, // slot 1 → slot 3
            {52.5f, 70f, 62.5f, 52f, 1, 4}, // slot 1 → slot 4
            {22.5f, 52f, 22.5f, 24f, 2, 5}, // slot 2 → slot 5
            {42.5f, 42f, 42.5f, 14f, 3, 6}, // slot 3 → slot 6
            {62.5f, 52f, 62.5f, 24f, 4, 7}, // slot 4 → slot 7
        };
        private final Drawable connDrawable;
        private final IntPredicate unlockedChecker;

        ConnectionsActor(Drawable connDrawable, IntPredicate unlockedChecker) {
            this.connDrawable = connDrawable;
            this.unlockedChecker = unlockedChecker;
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            if (connDrawable == null) return;
            for (float[] conn : CONNECTIONS) {
                boolean lit = unlockedChecker != null
                    && unlockedChecker.test((int)conn[4])
                    && unlockedChecker.test((int)conn[5]);
                Color lc = lit ? Color.WHITE : DIM_COLOR;
                batch.setColor(lc.r, lc.g, lc.b, lc.a * parentAlpha);
                float dx = conn[2] - conn[0];
                float dy = conn[3] - conn[1];
                float len = (float) Math.sqrt(dx * dx + dy * dy);
                float angle = (float)(Math.atan2(dy, dx) * 180f / Math.PI);
                batch.flush();
                Matrix4 prev = batch.getTransformMatrix().cpy();
                Matrix4 mat = new Matrix4(prev);
                mat.translate(getX() + conn[0], getY() + conn[1], 0);
                mat.rotate(0, 0, 1, angle);
                batch.setTransformMatrix(mat);
                connDrawable.draw(batch, 0, -THICKNESS * 0.5f, len, THICKNESS);
                batch.flush();
                batch.setTransformMatrix(prev);
            }
            batch.setColor(Color.WHITE);
        }
    }

    public void disposeOwnedTextures() {
        if (ownedTextures == null) return;
        for (Texture t : ownedTextures) {
            try { t.dispose(); } catch (Exception ignored) {}
        }
        ownedTextures.clear();
    }
}
