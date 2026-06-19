package com.github.MichalKC.manylands.ui.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.github.MichalKC.manylands.component.Inventory;
import com.github.MichalKC.manylands.component.Item;
import com.github.MichalKC.manylands.input.Command;
import com.github.MichalKC.manylands.input.UiEvent;
import com.github.MichalKC.manylands.ui.model.GameViewModel;

public class ShopView extends View<GameViewModel> {

    private static final float WINDOW_W = 222f;
    private static final float WINDOW_H = 152f;
    private static final int SHOP_ROWS = 3;
    private static final int SHOP_COLS = 8;
    private static final int INV_ROWS = 2;
    private static final int INV_COLS = 8;
    private static final int SHOP_SIZE = SHOP_ROWS * SHOP_COLS;

    private static final int ZONE_INV = 0;
    private static final int ZONE_SHOP = 1;
    private static final int ZONE_CONFIRM = 2;

    private final Array<Texture> ownedTextures = new Array<>();
    private final Array<ItemSlot> shopSlots = new Array<>();
    private final Array<ItemSlot> inventorySlots = new Array<>();
    private final Item[] sellSlots = new Item[SHOP_SIZE];

    private Runnable closeCallback;
    private Inventory playerInventory;
    private String storeName;

    private Image playerSelectionFrame;
    private Image shopSelectionFrame;
    private Table confirmButton;
    private Table confirmStack;
    private Drawable confirmNotPressedDr;
    private Drawable confirmPressedDr;
    private int playerSelectedSlot = 0;
    private int shopSelectedSlot = 0;
    private int focusZone = ZONE_INV;
    private Item heldItem = null;
    private boolean heldFromShop = false;
    private int heldSourceSlot = -1;
    private Item[] shopCatalog = new Item[0];
    private Item buyHeldItem = null;
    private int buyHeldSlot = -1;
    private Label infoLabel;
    private Image sellMarker;

    private static class ItemSlot extends Stack {
        final Image bg;
        final Image icon;

        ItemSlot(Drawable bgDrawable) {
            bg = new Image(bgDrawable);
            icon = new Image();
            icon.setVisible(false);
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

    public ShopView(Stage stage, Skin skin, GameViewModel viewModel) {
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

    public void showShop(String name, Inventory playerInv, java.util.List<Item> catalog) {
        this.storeName = name;
        this.playerInventory = playerInv;
        this.shopCatalog = (catalog != null && !catalog.isEmpty())
            ? catalog.toArray(new Item[0]) : new Item[0];
        java.util.Arrays.fill(sellSlots, null);
        heldItem = null;
        heldSourceSlot = -1;
        buyHeldItem = null;
        buyHeldSlot = -1;
        focusZone = ZONE_INV;
        playerSelectedSlot = 0;
        shopSelectedSlot = 0;
        buildLayout();
        setVisible(true);
        toFront();
        Stage st = getStage();
        float sw = st != null ? st.getWidth() : 320f;
        float sh = st != null ? st.getHeight() : 180f;
        setPosition((sw - WINDOW_W) * 0.5f, (sh - WINDOW_H) * 0.5f);
        invalidateHierarchy();
        validate();
        refreshItemIcons();
        refreshSelectionFrame();
    }

    public void hideShop() {
        // Return held + any items left in the sell area back to the player's inventory.
        if (playerInventory != null) {
            if (heldItem != null) {
                playerInventory.addItem(heldItem);
                heldItem = null;
                heldSourceSlot = -1;
            }
            for (int i = 0; i < sellSlots.length; i++) {
                if (sellSlots[i] != null) {
                    playerInventory.addItem(sellSlots[i]);
                    sellSlots[i] = null;
                }
            }
        }
        buyHeldItem = null;
        buyHeldSlot = -1;
        setVisible(false);
        disposeOwnedTextures();
    }

    private void buildLayout() {
        disposeOwnedTextures();
        shopSlots.clear();
        inventorySlots.clear();
        playerSelectionFrame = null;
        shopSelectionFrame = null;
        confirmButton = null;
        confirmStack = null;
        confirmNotPressedDr = null;
        confirmPressedDr = null;
        buyHeldItem = null;
        buyHeldSlot = -1;
        infoLabel = null;
        clearChildren();

        setSize(WINDOW_W, WINDOW_H);
        align(Align.top);
        pad(6f, 6f, 4f, 4f);

        Drawable outerBg = safeNinePatch9("ui/inventory/storeBackground.9.png");
        if (outerBg == null) outerBg = safeDrawable("ui/inventory/inventoryBackground.png");
        if (outerBg == null) outerBg = safeSkinDrawable("frame");
        if (outerBg != null) setBackground(outerBg);

        // Left column: shop grid, inventory grid
        Table left = new Table();
        left.align(Align.top);
        left.add(buildShopSection()).fillX().height(75f).padBottom(4f).row();
        left.add(buildInventorySection()).fillX().height(59f);

        // Right column: info panel on top, sell button below
        Table right = new Table();
        right.align(Align.top);
        right.add(buildInfoPanel()).width(55f).height(105f).padBottom(4f).row();
        right.add(buildConfirmSection()).fillX().height(16f);

        add(left).top().padRight(4f);
        add(right).top();
    }

    private Table buildInfoPanel() {
        Table panel = new Table();
        panel.align(Align.top);
        panel.pad(3f);

        Drawable bg = safeNinePatch9("ui/inventory/inventorySkillsText.9.png");
        if (bg == null) bg = safeNinePatch9("ui/inventory/inventoryInsideBack.9.png");
        if (bg != null) panel.setBackground(bg);

        try {
            infoLabel = new Label("", skin, "small");
            infoLabel.setFontScale(0.4f);
            infoLabel.setWrap(true);
            infoLabel.setColor(Color.WHITE);
            panel.add(infoLabel).width(49f).top().expand().fill();
        } catch (Exception ignored) {}
        return panel;
    }

    private void updateInfoPanel() {
        if (infoLabel == null) return;
        StringBuilder sb = new StringBuilder();

        Item item = getSelectedItem();
        if (item != null) {
            sb.append(item.getName()).append("\n\n");

            if (focusZone == ZONE_SHOP) {
                if (item.isBuyable()) {
                    sb.append("Buy: ").append(item.getPrice()).append(" coins\n");
                }
            } else if (focusZone == ZONE_INV) {
                if (item.isSellable()) {
                    sb.append("Sell: ").append(item.getValue()).append(" coins\n");
                } else {
                    sb.append("Not sellable\n");
                }
            }

            // Wearable info
            if (item.getWear() != null && !item.getWear().isEmpty()) {
                String slotName = item.getWear();
                switch (slotName) {
                    case "head": slotName = "Head"; break;
                    case "chest": slotName = "Chest"; break;
                    case "gloves": slotName = "Gloves"; break;
                    case "weapon": slotName = "Weapon"; break;
                    case "specialObject": slotName = "Special"; break;
                    case "boots": slotName = "Boots"; break;
                }
                sb.append("Equip: ").append(slotName).append("\n");
            }

            // Stats
            boolean hasStats = false;
            sb.append("\n");
            if (item.isEat()) { sb.append("Eat: HP +").append(item.getPlusHP()).append("\n"); hasStats = true; }
            else if (item.getPlusHP() != 0) { sb.append("HP +").append(item.getPlusHP()).append("\n"); hasStats = true; }
            if (item.getPlusAttack1() != 0) { sb.append("ATK1 +").append(item.getPlusAttack1()).append("\n"); hasStats = true; }
            if (item.getPlusAttack2() != 0) { sb.append("ATK2 +").append(item.getPlusAttack2()).append("\n"); hasStats = true; }
            if (item.getPlusAttack3() != 0) { sb.append("ATK3 +").append(item.getPlusAttack3()).append("\n"); hasStats = true; }
            if (item.getPlusDefense() != 0) { sb.append("DEF +").append(item.getPlusDefense()).append("\n"); hasStats = true; }
            if (item.getPlusSpeed() != 0) { sb.append("SPD +").append(item.getPlusSpeed()).append("\n"); hasStats = true; }
            if (!hasStats) sb.append("No bonuses");
        }

        // Always show profit at bottom (items in sell slots + held item if picked from shop)
        int sellTotal = 0;
        for (int i = 0; i < sellSlots.length; i++) {
            if (sellSlots[i] != null && sellSlots[i].isSellable()) sellTotal += sellSlots[i].getValue();
        }
        if (heldFromShop && heldItem != null && heldItem.isSellable()) sellTotal += heldItem.getValue();
        sb.append("\n\nProfit: ").append(sellTotal).append(" coins");

        infoLabel.setText(sb.toString());
    }

    private Item getSelectedItem() {
        if (focusZone == ZONE_SHOP) {
            if (shopCatalog != null && shopSelectedSlot < shopCatalog.length) {
                return shopCatalog[shopSelectedSlot];
            }
            if (shopSelectedSlot < sellSlots.length) return sellSlots[shopSelectedSlot];
        } else if (focusZone == ZONE_INV) {
            Item[] inv = playerInventory != null ? playerInventory.getSlots() : null;
            if (inv != null && playerSelectedSlot < inv.length) return inv[playerSelectedSlot];
        }
        return null;
    }

    private Table buildShopSection() {
        Table section = new Table();
        section.align(Align.top);
        section.pad(4f);

        Drawable bg = safeNinePatch9("ui/inventory/storeSellerInside.9.png");
        if (bg == null) bg = safeNinePatch9("ui/inventory/inventoryInsideBack.9.png");
        if (bg != null) section.setBackground(bg);

        try {
            Label title = new Label(storeName != null ? storeName.toUpperCase() : "SHOP", skin, "small");
            title.setFontScale(0.6f);
            title.setColor(new Color(1f, 0.85f, 0.3f, 1f));
            section.add(title).padBottom(3f).row();
        } catch (Exception ignored) {
            section.add().height(8f).row();
        }

        Table grid = new Table();
        Drawable shopSlotBg = safeDrawable("ui/inventory/9SlicedObjectsslot11.png");
        for (int row = 0; row < SHOP_ROWS; row++) {
            for (int col = 0; col < SHOP_COLS; col++) {
                final int idx = row * SHOP_COLS + col;
                ItemSlot slot = new ItemSlot(shopSlotBg != null ? shopSlotBg : safeSkinDrawable("frame"));
                slot.setTouchable(Touchable.enabled);
                onClick(slot, () -> {
                    focusZone = ZONE_SHOP;
                    shopSelectedSlot = idx;
                    activateSlot();
                    refreshSelectionFrame();
                });
                shopSlots.add(slot);
                grid.add(slot).size(14f, 14f).pad(1f);
            }
            grid.row();
        }
        section.add(grid).top();

        Drawable shopSelDr = safeDrawable("ui/inventory/inputs10.png");
        if (shopSelDr != null) {
            shopSelectionFrame = new Image(shopSelDr);
            shopSelectionFrame.setTouchable(Touchable.disabled);
            shopSelectionFrame.setVisible(false);
        }
        return section;
    }

    private Table buildInventorySection() {
        Table section = new Table();
        section.align(Align.top);
        section.pad(4f);

        Drawable bg = safeNinePatch9("ui/inventory/storePlayerInside.9.png");
        if (bg == null) bg = safeNinePatch9("ui/inventory/inventoryInsideBack.9.png");
        if (bg != null) section.setBackground(bg);

        try {
            Label title = new Label("YOUR INVENTORY", skin, "small");
            title.setFontScale(0.55f);
            section.add(title).padBottom(3f).row();
        } catch (Exception ignored) {
            section.add().height(7f).row();
        }

        Table grid = new Table();
        Drawable invSlotBg = safeDrawable("ui/inventory/9SlicedObjectsslot10.png");
        for (int row = 0; row < INV_ROWS; row++) {
            for (int col = 0; col < INV_COLS; col++) {
                final int idx = row * INV_COLS + col;
                ItemSlot slot = new ItemSlot(invSlotBg != null ? invSlotBg : safeSkinDrawable("frame"));
                slot.setTouchable(Touchable.enabled);
                onClick(slot, () -> {
                    focusZone = ZONE_INV;
                    playerSelectedSlot = idx;
                    activateSlot();
                    refreshSelectionFrame();
                });
                inventorySlots.add(slot);
                grid.add(slot).size(14f, 14f).pad(1f);
            }
            grid.row();
        }
        section.add(grid).top();

        Drawable playerSelDr = safeDrawable("ui/inventory/inputs12.png");
        if (playerSelDr != null) {
            playerSelectionFrame = new Image(playerSelDr);
            playerSelectionFrame.setTouchable(Touchable.disabled);
            playerSelectionFrame.setVisible(false);
        }
        return section;
    }

    private Table buildConfirmSection() {
        Table section = new Table();
        section.align(Align.center);

        confirmNotPressedDr = safeNinePatch9("ui/inventory/confirmButtonPressed.9.png");
        confirmPressedDr = safeNinePatch9("ui/inventory/confirmButtonNotPressed.9.png");
        Drawable initial = confirmNotPressedDr != null ? confirmNotPressedDr : safeSkinDrawable("frame");

        // 9-patch button: Table sized to its label content; the NinePatchDrawable
        // background stretches via its 9-patch guides to wrap the label perfectly.
        confirmButton = new Table() {
            @Override public void invalidateHierarchy() { invalidate(); }
        };
        if (initial != null) confirmButton.setBackground(initial);
        confirmButton.setTouchable(Touchable.enabled);
        try {
            Label label = new Label("SELL", skin, "small");
            label.setFontScale(0.45f);
            label.setAlignment(Align.center);
            // Small inner padding so the 9-patch hugs the text snugly.
            confirmButton.add(label).pad(1f, 3f, 1f, 3f);
        } catch (Exception ignored) {
        }
        onClick(confirmButton, () -> {
            focusZone = ZONE_CONFIRM;
            activateSlot();
            refreshSelectionFrame();
        });

        confirmStack = confirmButton;
        section.add(confirmButton);

        Drawable sellMarkerDr = safeNinePatch9("ui/inventory/shopSellButtonMarker.9.png");
        if (sellMarkerDr != null) {
            sellMarker = new Image(sellMarkerDr);
            sellMarker.setTouchable(Touchable.disabled);
            sellMarker.setVisible(false);
        }
        return section;
    }

    /** Pickup or place the held item at the active slot, buy a catalog item, or confirm sell. */
    private void activateSlot() {
        // ZONE_CONFIRM: deselect buy selection, or sell-queue logic, or confirm sell
        if (focusZone == ZONE_CONFIRM) {
            if (buyHeldItem != null) {
                buyHeldItem = null;
                buyHeldSlot = -1;
                refreshItemIcons();
                return;
            }
            if (heldItem != null) {
                if (heldItem.isSellable()) {
                    for (int i = 0; i < sellSlots.length; i++) {
                        if (sellSlots[i] == null) {
                            sellSlots[i] = heldItem;
                            heldItem = null;
                            break;
                        }
                    }
                    // Fall through to confirmSell() to sell immediately
                } else {
                    Item[] inv = playerInventory != null ? playerInventory.getSlots() : null;
                    if (inv != null) {
                        for (int i = 0; i < inv.length; i++) {
                            if (inv[i] == null) { inv[i] = heldItem; break; }
                        }
                    }
                    heldItem = null;
                    refreshItemIcons();
                    return;
                }
            }
            confirmSell();
            return;
        }

        // ZONE_SHOP: catalog slot = select/deselect; non-catalog slot = deselect buyHeldItem
        if (focusZone == ZONE_SHOP) {
            boolean isCatalog = shopCatalog != null
                && shopSelectedSlot < shopCatalog.length
                && shopCatalog[shopSelectedSlot] != null;
            if (isCatalog) {
                if (heldItem == null) {
                    if (buyHeldSlot == shopSelectedSlot) {
                        buyHeldItem = null;
                        buyHeldSlot = -1;
                    } else {
                        buyHeldItem = shopCatalog[shopSelectedSlot];
                        buyHeldSlot = shopSelectedSlot;
                    }
                    refreshItemIcons();
                }
                return;
            }
            // Non-catalog shop slot clicked while holding a catalog item: deselect
            if (buyHeldItem != null) {
                buyHeldItem = null;
                buyHeldSlot = -1;
                refreshItemIcons();
                return;
            }
        }

        // ZONE_INV while holding a catalog item: purchase, or deselect if not enough coins
        if (focusZone == ZONE_INV && buyHeldItem != null) {
            int price = buyHeldItem.getPrice();
            if (playerInventory != null && viewModel.getCoins() >= price) {
                Item purchased = new Item(
                    buyHeldItem.getId(), buyHeldItem.getName(),
                    buyHeldItem.getTexturePath(), buyHeldItem.getValue());
                if (buyHeldItem.getWear() != null) purchased.setWear(buyHeldItem.getWear());
                purchased.setEat(buyHeldItem.isEat());
                purchased.setRead(buyHeldItem.isRead());
                if (buyHeldItem.getDescription() != null) purchased.setDescription(buyHeldItem.getDescription());
                purchased.setPlusHP(buyHeldItem.getPlusHP());
                purchased.setPlusAttack1(buyHeldItem.getPlusAttack1());
                purchased.setPlusAttack2(buyHeldItem.getPlusAttack2());
                purchased.setPlusAttack3(buyHeldItem.getPlusAttack3());
                purchased.setPlusDefense(buyHeldItem.getPlusDefense());
                purchased.setPlusSpeed(buyHeldItem.getPlusSpeed());
                if (playerInventory.addItem(purchased)) {
                    viewModel.addCoins(-price);
                    try {
                        viewModel.getGame().getAudioService().playSound(
                            com.github.MichalKC.manylands.asset.SoundAsset.COIN);
                    } catch (Exception ignored) {}
                    buyHeldItem = null;
                    buyHeldSlot = -1;
                }
            } else {
                // Not enough coins (or no inventory): deselect
                buyHeldItem = null;
                buyHeldSlot = -1;
            }
            refreshItemIcons();
            return;
        }

        // Existing sell / inventory move logic
        if (playerInventory == null) return;
        Item[] invSlots = playerInventory.getSlots();
        if (invSlots == null) return;
        if (heldItem == null) {
            if (focusZone == ZONE_INV) {
                if (playerSelectedSlot < invSlots.length && invSlots[playerSelectedSlot] != null) {
                    heldItem = invSlots[playerSelectedSlot];
                    invSlots[playerSelectedSlot] = null;
                    heldFromShop = false;
                    heldSourceSlot = playerSelectedSlot;
                }
            } else { // ZONE_SHOP non-catalog
                if (shopSelectedSlot < sellSlots.length && sellSlots[shopSelectedSlot] != null) {
                    heldItem = sellSlots[shopSelectedSlot];
                    sellSlots[shopSelectedSlot] = null;
                    heldFromShop = true;
                    heldSourceSlot = shopSelectedSlot;
                }
            }
        } else {
            if (focusZone == ZONE_INV) {
                // Dropping on inventory side
                if (playerSelectedSlot < invSlots.length) {
                    Item displaced = invSlots[playerSelectedSlot];
                    invSlots[playerSelectedSlot] = heldItem;
                    if (displaced != null) {
                        // True swap: displaced item goes back to the source slot
                        if (heldFromShop && heldSourceSlot >= 0
                                && heldSourceSlot < sellSlots.length) {
                            if (!displaced.isSellable()) {
                                // Cannot place a non-sellable item in sell area: undo
                                invSlots[playerSelectedSlot] = displaced;
                                heldItem = null;
                                heldSourceSlot = -1;
                                heldFromShop = false;
                                refreshItemIcons();
                                updateInfoPanel();
                                return;
                            }
                            sellSlots[heldSourceSlot] = displaced;
                        } else if (!heldFromShop && heldSourceSlot >= 0
                                && heldSourceSlot < invSlots.length) {
                            invSlots[heldSourceSlot] = displaced;
                        } else {
                            // No valid source: pick up displaced instead
                            heldItem = displaced;
                            heldSourceSlot = playerSelectedSlot;
                            heldFromShop = false;
                            refreshItemIcons();
                            updateInfoPanel();
                            return;
                        }
                    }
                    heldItem = null;
                    heldSourceSlot = -1;
                    heldFromShop = false;
                }
            } else { // ZONE_SHOP non-catalog
                // Dropping on shop side
                if (!heldItem.isSellable()) return;
                if (shopSelectedSlot < sellSlots.length) {
                    Item displaced = sellSlots[shopSelectedSlot];
                    sellSlots[shopSelectedSlot] = heldItem;
                    if (displaced != null) {
                        if (heldFromShop && heldSourceSlot >= 0
                                && heldSourceSlot < sellSlots.length) {
                            sellSlots[heldSourceSlot] = displaced;
                        } else if (!heldFromShop && heldSourceSlot >= 0
                                && heldSourceSlot < invSlots.length) {
                            invSlots[heldSourceSlot] = displaced;
                        } else {
                            heldItem = displaced;
                            heldSourceSlot = shopSelectedSlot;
                            heldFromShop = true;
                            refreshItemIcons();
                            updateInfoPanel();
                            return;
                        }
                    }
                    heldItem = null;
                    heldSourceSlot = -1;
                    heldFromShop = false;
                }
            }
        }
        refreshItemIcons();
        updateInfoPanel();
    }

    private void confirmSell() {
        // Flash pressed visual; invalidateHierarchy() is overridden to only
        // invalidate self, so the parent never relayouts and button size is stable.
        if (confirmButton != null && confirmPressedDr != null && confirmNotPressedDr != null) {
            confirmButton.setBackground(confirmPressedDr);
            com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
                @Override
                public void run() {
                    if (confirmButton != null) confirmButton.setBackground(confirmNotPressedDr);
                }
            }, 0.12f);
        }
        int total = 0;
        boolean anySold = false;
        if (heldItem != null && heldItem.isSellable()) {
            total += Math.max(0, heldItem.getValue());
            heldItem = null;
            heldSourceSlot = -1;
            anySold = true;
        }
        for (int i = 0; i < sellSlots.length; i++) {
            Item it = sellSlots[i];
            if (it != null) {
                total += Math.max(0, it.getValue());
                sellSlots[i] = null;
                anySold = true;
            }
        }
        if (anySold && total > 0) {
            viewModel.addCoins(total);
            try {
                viewModel.getGame().getAudioService().playSound(
                    com.github.MichalKC.manylands.asset.SoundAsset.SELL);
            } catch (Exception ignored) {
            }
        }
        heldFromShop = false;
        refreshItemIcons();
        updateInfoPanel();
    }

    private void refreshSelectionFrame() {
        if (playerSelectionFrame != null && inventorySlots != null) {
            if (focusZone == ZONE_INV && playerSelectedSlot >= 0 && playerSelectedSlot < inventorySlots.size) {
                com.badlogic.gdx.scenes.scene2d.Actor slot = inventorySlots.get(playerSelectedSlot);
                Vector2 v = new Vector2(0, 0);
                slot.localToActorCoordinates(this, v);
                if (playerSelectionFrame.getParent() != this) addActor(playerSelectionFrame);
                playerSelectionFrame.setSize(slot.getWidth(), slot.getHeight());
                playerSelectionFrame.setPosition(v.x, v.y);
                playerSelectionFrame.setVisible(true);
                playerSelectionFrame.toFront();
            } else {
                playerSelectionFrame.setVisible(false);
            }
        }
        if (shopSelectionFrame != null && shopSlots != null) {
            if (focusZone == ZONE_SHOP && shopSelectedSlot >= 0 && shopSelectedSlot < shopSlots.size) {
                com.badlogic.gdx.scenes.scene2d.Actor slot = shopSlots.get(shopSelectedSlot);
                Vector2 v = new Vector2(0, 0);
                slot.localToActorCoordinates(this, v);
                if (shopSelectionFrame.getParent() != this) addActor(shopSelectionFrame);
                shopSelectionFrame.setSize(slot.getWidth(), slot.getHeight());
                shopSelectionFrame.setPosition(v.x, v.y);
                shopSelectionFrame.setVisible(true);
                shopSelectionFrame.toFront();
            } else {
                shopSelectionFrame.setVisible(false);
            }
        }
        if (sellMarker != null) {
            if (focusZone == ZONE_CONFIRM && confirmButton != null) {
                Vector2 cv = new Vector2(0, 0);
                confirmButton.localToActorCoordinates(this, cv);
                if (sellMarker.getParent() != this) addActor(sellMarker);
                sellMarker.setSize(confirmButton.getWidth() + 4f, confirmButton.getHeight() + 4f);
                sellMarker.setPosition(cv.x - 2f, cv.y - 2f);
                sellMarker.setVisible(true);
                sellMarker.toFront();
            } else {
                sellMarker.setVisible(false);
            }
        }
        updateInfoPanel();
    }

    @Override
    public void onLeft() {
        if (!isVisible()) return;
        if (focusZone == ZONE_CONFIRM) {
            focusZone = ZONE_INV;
            // go back to last col of last row
            int lastRow = INV_ROWS - 1;
            playerSelectedSlot = Math.min(lastRow * INV_COLS + INV_COLS - 1, inventorySlots.size - 1);
        } else if (focusZone == ZONE_SHOP) {
            if (shopSelectedSlot % SHOP_COLS > 0) shopSelectedSlot--;
        } else if (focusZone == ZONE_INV) {
            if (playerSelectedSlot % INV_COLS > 0) playerSelectedSlot--;
        }
        refreshSelectionFrame();
    }

    @Override
    public void onRight() {
        if (!isVisible()) return;
        if (focusZone == ZONE_SHOP) {
            if (shopSelectedSlot % SHOP_COLS < SHOP_COLS - 1 && shopSelectedSlot + 1 < shopSlots.size) shopSelectedSlot++;
        } else if (focusZone == ZONE_INV) {
            if (playerSelectedSlot % INV_COLS < INV_COLS - 1 && playerSelectedSlot + 1 < inventorySlots.size) {
                playerSelectedSlot++;
            } else {
                focusZone = ZONE_CONFIRM;
            }
        }
        refreshSelectionFrame();
    }

    @Override
    public void onUp() {
        if (!isVisible()) return;
        if (focusZone == ZONE_CONFIRM) {
            focusZone = ZONE_INV;
            int target = (INV_ROWS - 1) * INV_COLS;
            if (target < inventorySlots.size) playerSelectedSlot = target;
        } else if (focusZone == ZONE_SHOP) {
            if (shopSelectedSlot >= SHOP_COLS) {
                shopSelectedSlot -= SHOP_COLS;
            }
        } else { // ZONE_INV
            if (playerSelectedSlot >= INV_COLS) {
                playerSelectedSlot -= INV_COLS;
            } else {
                focusZone = ZONE_SHOP;
                shopSelectedSlot = (SHOP_ROWS - 1) * SHOP_COLS + Math.min(playerSelectedSlot % INV_COLS, SHOP_COLS - 1);
            }
        }
        refreshSelectionFrame();
    }

    @Override
    public void onDown() {
        if (!isVisible()) return;
        if (focusZone == ZONE_SHOP) {
            if (shopSelectedSlot + SHOP_COLS < shopSlots.size) {
                shopSelectedSlot += SHOP_COLS;
            } else {
                focusZone = ZONE_INV;
                playerSelectedSlot = Math.min(shopSelectedSlot % SHOP_COLS, INV_COLS - 1);
            }
        } else if (focusZone == ZONE_INV) {
            if (playerSelectedSlot + INV_COLS < inventorySlots.size) {
                playerSelectedSlot += INV_COLS;
            }
        }
        refreshSelectionFrame();
    }

    @Override
    public void onSelect() {
        if (!isVisible()) return;
        activateSlot();
    }

    private void refreshItemIcons() {
        Item[] playerSlots = playerInventory != null ? playerInventory.getSlots() : null;
        for (int i = 0; i < inventorySlots.size; i++) {
            Item item = (playerSlots != null && i < playerSlots.length) ? playerSlots[i] : null;
            inventorySlots.get(i).setItemIcon(item != null ? safeDrawable(item.getTexturePath()) : null);
            inventorySlots.get(i).bg.setColor(Color.WHITE);
        }
        // Tint target inventory slot gold when a catalog item is selected for purchase
        if (buyHeldItem != null && focusZone == ZONE_INV && playerSelectedSlot < inventorySlots.size) {
            inventorySlots.get(playerSelectedSlot).bg.setColor(new Color(1f, 0.85f, 0.3f, 1f));
        }
        for (int i = 0; i < shopSlots.size; i++) {
            boolean isCatalog = shopCatalog != null && i < shopCatalog.length && shopCatalog[i] != null;
            if (isCatalog) {
                shopSlots.get(i).setItemIcon(safeDrawable(shopCatalog[i].getTexturePath()));
                // Selected catalog slot = bright gold; others = warm tint
                shopSlots.get(i).bg.setColor(i == buyHeldSlot
                    ? new Color(1f, 0.8f, 0.1f, 1f)
                    : new Color(0.95f, 0.88f, 0.65f, 1f));
            } else {
                Item item = (i < sellSlots.length) ? sellSlots[i] : null;
                shopSlots.get(i).setItemIcon(item != null ? safeDrawable(item.getTexturePath()) : null);
                shopSlots.get(i).bg.setColor(Color.WHITE);
            }
        }
        // Render the held player item as the icon under the cursor, tinted green
        if (heldItem != null) {
            if (focusZone == ZONE_INV && playerSelectedSlot < inventorySlots.size) {
                inventorySlots.get(playerSelectedSlot).setItemIcon(safeDrawable(heldItem.getTexturePath()));
                inventorySlots.get(playerSelectedSlot).bg.setColor(new Color(0.5f, 1f, 0.5f, 1f));
            } else if (focusZone == ZONE_SHOP && shopSelectedSlot < shopSlots.size) {
                boolean isCatalog = shopCatalog != null && shopSelectedSlot < shopCatalog.length
                    && shopCatalog[shopSelectedSlot] != null;
                if (!isCatalog) {
                    shopSlots.get(shopSelectedSlot).setItemIcon(safeDrawable(heldItem.getTexturePath()));
                    shopSlots.get(shopSelectedSlot).bg.setColor(new Color(0.5f, 1f, 0.5f, 1f));
                }
            }
        }
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

    @Override
    public boolean handle(Event event) {
        if (!isVisible()) return false;
        if (event instanceof UiEvent uiEvent) {
            Command cmd = uiEvent.getCommand();
            if (cmd == Command.CANCEL || cmd == Command.INTERACT) {
                if (closeCallback != null) closeCallback.run();
                return true;
            }
        }
        return super.handle(event);
    }

    public void disposeOwnedTextures() {
        for (Texture t : ownedTextures) {
            try { t.dispose(); } catch (Exception ignored) {}
        }
        ownedTextures.clear();
    }
}
