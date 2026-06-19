package com.github.MichalKC.manylands.ui.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Scaling;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.IntPredicate;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.github.MichalKC.manylands.input.Command;
import com.github.MichalKC.manylands.input.UiEvent;
import com.github.MichalKC.manylands.ui.model.GameViewModel;

public class SkillsShopView extends View<GameViewModel> {

    private static final int SKILL_COUNT = 8;
    private static final int COLS = 4;
    private static final int ROWS = 2;
    private static final float WINDOW_W = 130f;
    private static final float WINDOW_H = 78f;
    private static final float SLOT_SIZE = 14f;

    /** Maps slot index (0–7) to HUD icon index, matching InventoryView.SKILL_HUD_ICONS. */
    private static final int[] SKILL_HUD_ICONS = {3, 4, 0, 5, 7, 1, 2, 6};
    private static final Color DIM_COLOR = new Color(0.45f, 0.45f, 0.45f, 1f);

    private final Array<Texture> ownedTextures = new Array<>();
    private final Array<SkillSlot> skillSlots = new Array<>();

    private int cursorPos = 0;
    private int selectedSkill = -1;
    private Runnable closeCallback;
    private Consumer<Integer> onSkillUnlocked;
    private IntPredicate skillBoughtChecker;

    private Label descLabel;
    private Label buyLabel;
    private Label priceLabel;
    private Table buyButton;
    private Stack buyStack;
    private Image cursorMarker;
    private Drawable markerDrawable;
    private Drawable buyNotPressedDr;
    private Drawable buyPressedDr;
    private final Set<Integer> boughtSlots = new HashSet<>();

    private static class SkillSlot extends Stack {
        final Image bg;
        final Image icon;
        final Image blocked;

        SkillSlot(Drawable bgDrawable, Drawable blockedDrawable) {
            bg = new Image(bgDrawable);
            icon = new Image();
            icon.setScaling(Scaling.fit);
            icon.setVisible(false);
            blocked = new Image();
            blocked.setScaling(Scaling.fit);
            if (blockedDrawable != null) blocked.setDrawable(blockedDrawable);
            blocked.setVisible(false);
            add(bg);
            Container<Image> iconWrapper = new Container<>(icon);
            iconWrapper.pad(1f);
            add(iconWrapper);
            Container<Image> blockedWrapper = new Container<>(blocked);
            blockedWrapper.pad(1f);
            add(blockedWrapper);
        }

        void setIcon(Drawable iconDrawable) {
            if (iconDrawable != null) {
                icon.setDrawable(iconDrawable);
                icon.setVisible(true);
            } else {
                icon.setVisible(false);
            }
        }
    }

    public SkillsShopView(Stage stage, Skin skin, GameViewModel viewModel) {
        super(stage, skin, viewModel);
    }

    @Override
    protected void setupUI() {
        setVisible(false);
    }

    @Override
    protected void setupPropertyChanges() {
    }

    public void setCloseCallback(Runnable callback) {
        this.closeCallback = callback;
    }

    public void setSkillUnlockCallback(Consumer<Integer> callback) {
        this.onSkillUnlocked = callback;
    }

    public void setSkillBoughtChecker(IntPredicate checker) {
        this.skillBoughtChecker = checker;
    }

    public void showShop() {
        cursorPos = 0;
        selectedSkill = -1;
        boughtSlots.clear();
        if (skillBoughtChecker != null) {
            for (int i = 0; i < SKILL_COUNT; i++) {
                if (skillBoughtChecker.test(i)) boughtSlots.add(i);
            }
        }
        buildLayout();
        setVisible(true);
        toFront();
        Stage st = getStage();
        float sw = st != null ? st.getWidth() : 320f;
        float sh = st != null ? st.getHeight() : 180f;
        setPosition((sw - WINDOW_W) * 0.5f, (sh - WINDOW_H) * 0.5f);
        invalidateHierarchy();
        validate();
        refresh();
    }

    public void hideShop() {
        setVisible(false);
        disposeOwnedTextures();
    }

    private void buildLayout() {
        disposeOwnedTextures();
        skillSlots.clear();
        descLabel = null;
        buyLabel = null;
        priceLabel = null;
        buyButton = null;
        buyStack = null;
        cursorMarker = null;
        buyNotPressedDr = null;
        buyPressedDr = null;
        clearChildren();

        setSize(WINDOW_W, WINDOW_H);
        pad(5f, 5f, 5f, 5f);

        Drawable outerBg = safeNinePatch9("ui/inventory/skillsShopBacground.9.png");
        if (outerBg == null) outerBg = safeNinePatch9("ui/inventory/storeBackground.9.png");
        if (outerBg == null) outerBg = safeSkinDrawable("frame");
        if (outerBg != null) setBackground(outerBg);

        markerDrawable = safeNinePatch9("ui/inventory/shopSellButtonMarker.9.png");
        if (markerDrawable != null) {
            cursorMarker = new Image(markerDrawable);
            cursorMarker.setVisible(false);
            cursorMarker.setTouchable(Touchable.disabled);
        }

        add(buildSlotsSection()).expand().center().padRight(5f);

        Table right = new Table();
        right.align(Align.top);
        right.add(buildDescSection()).width(42f).height(50f).padBottom(4f).row();
        right.add(buildBuySection()).fillX();
        add(right).expandY().center();

        if (cursorMarker != null) addActor(cursorMarker);
    }

    private Table buildSlotsSection() {
        Table section = new Table();
        section.align(Align.center);
        section.pad(3f);

        Drawable slotBg = safeDrawable("ui/inventory/skilliconslothudempty1.png");
        Drawable blockedDr = safeDrawable("ui/inventory/skillBlocked.png");

        Table grid = new Table();
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                final int idx = row * COLS + col;
                SkillSlot slot = new SkillSlot(
                    slotBg != null ? slotBg : safeSkinDrawable("frame"),
                    blockedDr
                );
                slot.setTouchable(Touchable.enabled);
                onClick(slot, () -> {
                    if (!isSkillPurchasable(idx)) return;
                    cursorPos = idx;
                    selectedSkill = (selectedSkill == idx) ? -1 : idx;
                    refresh();
                });
                skillSlots.add(slot);
                grid.add(slot).size(SLOT_SIZE, SLOT_SIZE).pad(1f);
            }
            grid.row();
        }
        section.add(grid).row();
        try {
            priceLabel = new Label("", skin, "small");
            priceLabel.setFontScale(0.33f);
            priceLabel.setAlignment(Align.center);
            priceLabel.setColor(Color.WHITE);
            section.add(priceLabel).padTop(3f);
        } catch (Exception ignored) {}
        return section;
    }

    private Table buildDescSection() {
        Table panel = new Table();
        panel.align(Align.top);
        panel.pad(3f);

        Drawable bg = safeNinePatch9("ui/inventory/inventorySkillsText.9.png");
        if (bg == null) bg = safeNinePatch9("ui/inventory/inventoryInsideBack.9.png");
        if (bg != null) panel.setBackground(bg);

        try {
            descLabel = new Label("Select a skill.", skin, "small");
            descLabel.setFontScale(0.33f);
            descLabel.setWrap(true);
            descLabel.setColor(Color.WHITE);
            panel.add(descLabel).width(36f).top().expand().fill();
        } catch (Exception ignored) {}

        return panel;
    }

    private Table buildBuySection() {
        Table section = new Table();
        section.align(Align.center);

        buyNotPressedDr = safeNinePatch9("ui/inventory/skillsButtonNotPressed.9.png");
        buyPressedDr = safeNinePatch9("ui/inventory/skillsButtonPressed.9.png");
        Drawable initial = buyNotPressedDr != null ? buyNotPressedDr : safeSkinDrawable("frame");

        buyButton = new Table() {
            @Override public void invalidateHierarchy() { invalidate(); }
        };
        if (initial != null) buyButton.setBackground(initial);
        buyButton.setTouchable(Touchable.enabled);
        try {
            buyLabel = new Label("BUY", skin, "small");
            buyLabel.setFontScale(0.45f);
            buyLabel.setAlignment(Align.center);
            buyButton.add(buyLabel).pad(1f, 3f, 1f, 3f);
        } catch (Exception ignored) {}
        onClick(buyButton, this::buySelected);

        buyStack = new Stack();
        buyStack.add(buyButton);
        buyStack.setTouchable(Touchable.enabled);

        section.add(buyStack);
        return section;
    }

    private void refresh() {
        for (int i = 0; i < skillSlots.size; i++) {
            SkillSlot slot = skillSlots.get(i);
            boolean purchasable = isSkillPurchasable(i);
            boolean bought = boughtSlots.contains(i);
            boolean highlighted = (i == selectedSkill);
            int hudIdx = SKILL_HUD_ICONS[i];
            slot.setIcon(safeDrawable("ui/inventory/skilliconslothud" + hudIdx + ".png"));
            slot.icon.setColor(bought || highlighted ? Color.WHITE : DIM_COLOR);
            slot.blocked.setVisible(!purchasable);
            slot.blocked.setColor(Color.WHITE);
            slot.setTouchable(purchasable ? Touchable.enabled : Touchable.disabled);
        }
        updateBuyLabel();
        updatePriceLabel();
        updateDesc();
    }

    private void updateBuyLabel() {
        if (buyLabel == null) return;
        buyLabel.setText("BUY");
    }

    private void updatePriceLabel() {
        if (priceLabel == null) return;
        if (selectedSkill >= 0 && !boughtSlots.contains(selectedSkill)) {
            int cost = getSkillCost(selectedSkill);
            priceLabel.setText("Cost: " + cost + " coins");
        } else {
            priceLabel.setText("");
        }
    }

    private void updateDesc() {
        if (descLabel == null) return;
        int descIdx = (cursorPos < SKILL_COUNT) ? cursorPos : selectedSkill;
        if (descIdx < 0) {
            descLabel.setText("Select a skill.");
        } else {
            descLabel.setText(getSkillDescription(descIdx));
        }
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

    private static int getSkillCost(int idx) {
        if (idx <= 1) return 50;
        if (idx <= 4) return 75;
        return 100;
    }

    private void buySelected() {
        if (buyButton == null) return;
        if (buyPressedDr != null && buyNotPressedDr != null) {
            buyButton.setBackground(buyPressedDr);
            com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
                @Override
                public void run() {
                    if (buyButton != null) buyButton.setBackground(buyNotPressedDr);
                }
            }, 0.12f);
        }
        if (selectedSkill < 0 || boughtSlots.contains(selectedSkill)) return;
        int cost = getSkillCost(selectedSkill);
        if (viewModel.getCoins() < cost) return;
        int buying = selectedSkill;
        viewModel.addCoins(-cost);
        boughtSlots.add(buying);
        if (onSkillUnlocked != null) onSkillUnlocked.accept(buying);
        refresh();
    }

    @Override
    public void onLeft() {
        if (!isVisible()) return;
        if (cursorPos < SKILL_COUNT) {
            int row = cursorPos / COLS;
            cursorPos = findNextInRow(cursorPos, -1, row * COLS, (row + 1) * COLS);
        }
        refresh();
    }

    @Override
    public void onRight() {
        if (!isVisible()) return;
        if (cursorPos < SKILL_COUNT) {
            int row = cursorPos / COLS;
            cursorPos = findNextInRow(cursorPos, +1, row * COLS, (row + 1) * COLS);
        }
        refresh();
    }

    @Override
    public void onUp() {
        if (!isVisible()) return;
        if (cursorPos == SKILL_COUNT) {
            boolean found = false;
            for (int i = COLS; i < SKILL_COUNT; i++) {
                if (isSkillPurchasable(i)) { cursorPos = i; found = true; break; }
            }
            if (!found) {
                for (int i = 0; i < COLS; i++) {
                    if (isSkillPurchasable(i)) { cursorPos = i; break; }
                }
            }
        } else if (cursorPos >= COLS) {
            int candidate = cursorPos - COLS;
            if (isSkillPurchasable(candidate)) cursorPos = candidate;
        }
        refresh();
    }

    @Override
    public void onDown() {
        if (!isVisible()) return;
        if (cursorPos < COLS) {
            int candidate = cursorPos + COLS;
            if (candidate < SKILL_COUNT && isSkillPurchasable(candidate)) cursorPos = candidate;
            else cursorPos = SKILL_COUNT;
        } else if (cursorPos < SKILL_COUNT) {
            cursorPos = SKILL_COUNT;
        }
        refresh();
    }

    @Override
    public void onSelect() {
        if (!isVisible()) return;
        if (cursorPos < SKILL_COUNT) {
            if (cursorPos == selectedSkill && !boughtSlots.contains(cursorPos)) {
                selectedSkill = -1;
            } else {
                selectedSkill = cursorPos;
            }
            refresh();
        } else {
            buySelected();
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        updateCursorMarker();
    }

    private static final float MARKER_EXTEND = 1.5f;

    private void updateCursorMarker() {
        if (cursorMarker == null || !isVisible()) return;
        Actor target = null;
        if (cursorPos < SKILL_COUNT && skillSlots != null && cursorPos < skillSlots.size) {
            if (isSkillPurchasable(cursorPos)) target = skillSlots.get(cursorPos);
        } else if (cursorPos == SKILL_COUNT) {
            target = buyStack;
        }
        if (target == null) { cursorMarker.setVisible(false); return; }
        Vector2 pos = target.localToAscendantCoordinates(this, new Vector2(0, 0));
        cursorMarker.setBounds(
            pos.x - MARKER_EXTEND, pos.y - MARKER_EXTEND,
            target.getWidth() + MARKER_EXTEND * 2, target.getHeight() + MARKER_EXTEND * 2
        );
        cursorMarker.setVisible(true);
    }

    private boolean isSkillPurchasable(int i) {
        switch (i) {
            case 0: case 1: return true;
            case 2: return boughtSlots.contains(0);
            case 3: return boughtSlots.contains(0) || boughtSlots.contains(1);
            case 4: return boughtSlots.contains(1);
            case 5: return boughtSlots.contains(2);
            case 6: return boughtSlots.contains(3);
            case 7: return boughtSlots.contains(4);
            default: return false;
        }
    }

    private int findNextInRow(int from, int step, int rowStart, int rowEnd) {
        int next = from + step;
        while (next >= rowStart && next < rowEnd) {
            if (isSkillPurchasable(next)) return next;
            next += step;
        }
        return from;
    }

    @Override
    public boolean handle(Event event) {
        if (!isVisible()) return false;
        if (event instanceof UiEvent uiEvent) {
            Command cmd = uiEvent.getCommand();
            if (cmd == Command.CANCEL) {
                if (closeCallback != null) closeCallback.run();
                return true;
            }
        }
        return super.handle(event);
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

    private Drawable safeNinePatch9(String path) {
        try {
            Pixmap pm = new Pixmap(Gdx.files.internal(path));
            int fullW = pm.getWidth();
            int fullH = pm.getHeight();

            int stretchXStart = -1, stretchXEnd = -1;
            for (int x = 1; x < fullW - 1; x++) {
                int rgba = pm.getPixel(x, 0);
                if (isGuidePixel(rgba)) {
                    if (stretchXStart < 0) stretchXStart = x;
                    stretchXEnd = x;
                }
            }
            int stretchYStart = -1, stretchYEnd = -1;
            for (int y = 1; y < fullH - 1; y++) {
                int rgba = pm.getPixel(0, y);
                if (isGuidePixel(rgba)) {
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

    public void disposeOwnedTextures() {
        for (Texture t : ownedTextures) {
            try { t.dispose(); } catch (Exception ignored) {}
        }
        ownedTextures.clear();
    }
}
