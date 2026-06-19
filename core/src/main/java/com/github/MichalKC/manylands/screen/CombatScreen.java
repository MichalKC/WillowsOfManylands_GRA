package com.github.MichalKC.manylands.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.MichalKC.manylands.GdxGame;
import com.github.MichalKC.manylands.asset.MusicAsset;
import com.github.MichalKC.manylands.audio.AudioService;
import com.github.MichalKC.manylands.combat.*;

import com.github.MichalKC.manylands.component.Item;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.github.MichalKC.manylands.combat.CombatConstants.*;

public class CombatScreen extends ScreenAdapter {
    private enum Phase { START, DIALOG, MENU, FIGHT_ANIM, ACT_MENU, ITEM_MENU, MERCY_MENU, ENEMY_TURN, VICTORY, DEFEAT }

    private static final String[] MENU_LABELS = {"FIGHT", "ACT", "ITEM", "MERCY"};
    private static final String[] ACTS = {"Check", "Compliment", "Talk"};
    private static final String[] MERCY_OPTS = {"Spare", "Flee"};

    private final GdxGame game;
    private final Viewport viewport;
    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final GlyphLayout layout = new GlyphLayout();

    private CombatConfig config;
    private Texture enemyTexture;
    private Texture enemyDamageTexture;
    private float playerMaxHp;
    private Item[] inventoryItems = new Item[0];
    private final List<String> consumedItemIds = new ArrayList<>();

    private Phase phase;
    private int menuIdx;
    private float playerHp;
    private float enemyHp;
    private float enemyMaxHp;
    private String dialog;
    private int dialogCharIdx;
    private float dialogTimer;
    private Phase afterDialog;
    private int turnCount;
    private int mercyLevel;
    private int attackIndex;
    private float attackTimer;
    private AttackPattern currentAttack;
    private AttackPattern[] allPatterns;
    private final List<CombatProjectile> projectiles = new ArrayList<>();
    private float heartX, heartY;
    private float iFrameTimer;
    private float shakeTimer;
    private float fightAnimTimer;
    private boolean showDamageSprite;
    private float damageShowTimer;
    private float liquidTimer;
    private boolean enemyDimmed;

    private MusicAsset prevMusicAsset;

    private InputAdapter inputAdapter;

    public CombatScreen(GdxGame game) {
        this.game = game;
        this.viewport = new FitViewport(CANVAS_W, CANVAS_H);
        this.shapeRenderer = new ShapeRenderer();
        this.batch = new SpriteBatch();
        this.font = new BitmapFont();
        this.font.getData().setScale(1.2f);
        this.font.setColor(Color.WHITE);
    }

    public void configure(CombatConfig config, float playerCurrentHp, float playerMaxHp) {
        this.config = config;
        this.playerHp = playerCurrentHp;
        this.playerMaxHp = playerMaxHp;
    }

    public void setInventoryItems(Item[] slots) {
        List<Item> items = new ArrayList<>();
        if (slots != null) {
            for (Item item : slots) {
                if (item != null) items.add(item);
            }
        }
        this.inventoryItems = items.toArray(new Item[0]);
    }

    public Item[] getInventoryItems() {
        return inventoryItems;
    }

    public java.util.List<String> getConsumedItemIds() {
        return consumedItemIds;
    }

    @Override
    public void show() {
        if (config == null) return;
        AudioService audio = game.getAudioService();
        prevMusicAsset = audio.getCurrentMusicAsset();
        if (config.getMusicAsset() != null) {
            audio.playMusic(config.getMusicAsset());
        }
        enemyHp = config.getMaxHp();
        enemyMaxHp = config.getMaxHp();
        try {
            enemyTexture = new Texture(Gdx.files.internal(config.getImagePath()));
        } catch (Exception e) {
            enemyTexture = null;
        }
        if (config.getDamageImagePath() != null) {
            try {
                enemyDamageTexture = new Texture(Gdx.files.internal(config.getDamageImagePath()));
            } catch (Exception ignored) {}
        }
        allPatterns = AttackPatternFactory.createAll(config.getAttackPatterns());

        phase = Phase.START;
        menuIdx = 0;
        consumedItemIds.clear();
        dialog = config.getDialogLines().length > 0 ? config.getDialogLines()[0] : "";
        dialogCharIdx = 0;
        dialogTimer = 0;
        afterDialog = Phase.MENU;
        turnCount = 0;
        mercyLevel = 0;
        attackIndex = 0;
        attackTimer = 0;
        currentAttack = null;
        projectiles.clear();
        float pendingScreenX = game.getPendingCombatHeartScreenX();
        float pendingScreenY = game.getPendingCombatHeartScreenY();
        if (pendingScreenX >= 0 && pendingScreenY >= 0) {
            viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
            com.badlogic.gdx.math.Vector3 v = new com.badlogic.gdx.math.Vector3(pendingScreenX, pendingScreenY, 0);
            viewport.unproject(v);
            heartX = v.x;
            heartY = v.y;
        } else {
            float nx = game.getPendingCombatHeartNormX();
            float ny = game.getPendingCombatHeartNormY();
            if (nx >= 0 && ny >= 0) {
                viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
                int vx = viewport.getScreenX();
                int vy = viewport.getScreenY();
                int vw = viewport.getScreenWidth();
                int vh = viewport.getScreenHeight();
                float sx = vx + nx * vw;
                float sy = vy + ny * vh;
                com.badlogic.gdx.math.Vector3 v = new com.badlogic.gdx.math.Vector3(sx, sy, 0);
                viewport.unproject(v);
                heartX = v.x;
                heartY = v.y;
            } else {
                heartX = BOX_X + BOX_W / 2f;
                heartY = BOX_Y + BOX_H / 2f;
            }
        }
        float half = HEART_SIZE / 2f;
        heartX = Math.max(BOX_X + half + 2, Math.min(BOX_X + BOX_W - half - 2, heartX));
        heartY = Math.max(BOX_Y + half + 2, Math.min(BOX_Y + BOX_H - half - 2, heartY));
        iFrameTimer = 0;
        shakeTimer = 0;
        fightAnimTimer = 0;
        showDamageSprite = false;
        damageShowTimer = 0;
        enemyDimmed = false;

        inputAdapter = new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                Gdx.app.log("CombatScreen", "keyDown: " + keycode + " phase=" + phase);
                handleKey(keycode);
                return true;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (button != 0) return false;
                com.badlogic.gdx.math.Vector3 tmp = new com.badlogic.gdx.math.Vector3(screenX, screenY, 0);
                viewport.unproject(tmp);
                handleClick(tmp.x, tmp.y);
                return true;
            }
        };
        game.setInputProcessors(inputAdapter);
    }

    @Override
    public void hide() {
        if (enemyTexture != null) { enemyTexture.dispose(); enemyTexture = null; }
        if (enemyDamageTexture != null) { enemyDamageTexture.dispose(); enemyDamageTexture = null; }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }


    private void handleKey(int key) {
        switch (phase) {
            case START:
                if (isConfirm(key)) { phase = Phase.DIALOG; dialogCharIdx = 0; dialogTimer = 0; }
                else if (key == Input.Keys.ESCAPE) endWithResult(CombatResult.DEFEAT);
                break;
            case DIALOG:
                if (key == Input.Keys.ESCAPE) {
                    if (afterDialog != Phase.VICTORY) {
                        endWithResult(CombatResult.DEFEAT);
                    }
                    break;
                }
                if (isConfirm(key) || key == Input.Keys.SPACE) {
                    if (dialogCharIdx >= dialog.length()) {
                        if (afterDialog == Phase.VICTORY) { endWithResult(CombatResult.VICTORY); }
                        else if (afterDialog == Phase.ENEMY_TURN) startEnemyTurn();
                        else { phase = Phase.MENU; menuIdx = 0; }
                    } else {
                        dialogCharIdx = dialog.length();
                    }
                }
                break;
            case MENU:
                if (key == Input.Keys.LEFT || key == Input.Keys.A) menuIdx = Math.max(0, menuIdx - 1);
                else if (key == Input.Keys.RIGHT || key == Input.Keys.D) menuIdx = Math.min(MENU_LABELS.length - 1, menuIdx + 1);
                else if (isConfirm(key)) onMenuSelect();
                else if (key == Input.Keys.ESCAPE) endWithResult(CombatResult.DEFEAT);
                break;
            case ACT_MENU:
                if (key == Input.Keys.UP || key == Input.Keys.W) menuIdx = Math.max(0, menuIdx - 1);
                else if (key == Input.Keys.DOWN || key == Input.Keys.S) menuIdx = Math.min(ACTS.length - 1, menuIdx + 1);
                else if (key == Input.Keys.ESCAPE || key == Input.Keys.X) { phase = Phase.MENU; menuIdx = 1; }
                else if (isConfirm(key)) onActSelect();
                break;
            case ITEM_MENU:
                if (key == Input.Keys.UP || key == Input.Keys.W) menuIdx = Math.max(0, menuIdx - 1);
                else if (key == Input.Keys.DOWN || key == Input.Keys.S) menuIdx = Math.min(Math.max(0, inventoryItems.length - 1), menuIdx + 1);
                else if (key == Input.Keys.ESCAPE || key == Input.Keys.X) { phase = Phase.MENU; menuIdx = 2; }
                else if (isConfirm(key)) onItemSelect();
                break;
            case MERCY_MENU:
                if (key == Input.Keys.UP || key == Input.Keys.W) menuIdx = Math.max(0, menuIdx - 1);
                else if (key == Input.Keys.DOWN || key == Input.Keys.S) menuIdx = Math.min(MERCY_OPTS.length - 1, menuIdx + 1);
                else if (key == Input.Keys.ESCAPE || key == Input.Keys.X) { phase = Phase.MENU; menuIdx = 3; }
                else if (isConfirm(key)) onMercySelect();
                break;
            case ENEMY_TURN:
                if (key == Input.Keys.ESCAPE) endWithResult(CombatResult.DEFEAT);
                break;
            case VICTORY:
                if (isConfirm(key)) endWithResult(CombatResult.VICTORY);
                break;
            case DEFEAT:
                if (isConfirm(key)) endWithResult(CombatResult.DEFEAT);
                break;
            default: break;
        }
    }

    private boolean isConfirm(int key) {
        return key == Input.Keys.SPACE || key == Input.Keys.ENTER;
    }

    private void handleClick(float wx, float wy) {
        switch (phase) {
            case START:
                phase = Phase.DIALOG; dialogCharIdx = 0; dialogTimer = 0;
                break;
            case DIALOG:
                if (dialogCharIdx >= dialog.length()) {
                    if (afterDialog == Phase.VICTORY) { endWithResult(CombatResult.VICTORY); }
                    else if (afterDialog == Phase.ENEMY_TURN) startEnemyTurn();
                    else { phase = Phase.MENU; menuIdx = 0; }
                } else {
                    dialogCharIdx = dialog.length();
                }
                break;
            case VICTORY:
                endWithResult(CombatResult.VICTORY);
                break;
            case DEFEAT:
                endWithResult(CombatResult.DEFEAT);
                break;
            case MENU:
            case ACT_MENU:
            case ITEM_MENU:
            case MERCY_MENU: {
                int btnIdx = hitMenuButton(wx, wy);
                if (btnIdx >= 0) {
                    if (phase == Phase.MENU) {
                        menuIdx = btnIdx;
                        onMenuSelect();
                    } else {
                        switch (MENU_LABELS[btnIdx]) {
                            case "FIGHT": menuIdx = 0; onMenuSelect(); break;
                            case "ACT": phase = Phase.ACT_MENU; menuIdx = 0; break;
                            case "ITEM": phase = Phase.ITEM_MENU; menuIdx = 0; break;
                            case "MERCY": phase = Phase.MERCY_MENU; menuIdx = 0; break;
                        }
                    }
                    break;
                }

                int optIdx = hitSubOption(wx, wy);
                if (optIdx >= 0) {
                    menuIdx = optIdx;
                    if (phase == Phase.ACT_MENU) onActSelect();
                    else if (phase == Phase.ITEM_MENU) onItemSelect();
                    else if (phase == Phase.MERCY_MENU) onMercySelect();
                }
                break;
            }
            default: break;
        }
    }

    private int hitMenuButton(float wx, float wy) {
        float btnW = MENU_BTN_W, btnH = MENU_BTN_H;
        float totalW = MENU_LABELS.length * btnW + (MENU_LABELS.length - 1) * 12;
        float startX = (CANVAS_W - totalW) / 2f;
        float by = MENU_BTN_Y;
        for (int i = 0; i < MENU_LABELS.length; i++) {
            float bx = startX + i * (btnW + 12);
            if (wx >= bx && wx <= bx + btnW && wy >= by && wy <= by + btnH) {
                return i;
            }
        }
        return -1;
    }

    private int hitSubOption(float wx, float wy) {
        float pad = 14f;
        float tx = BOX_X + pad;
        float ty = BOX_Y + BOX_H - pad;
        if (wx < tx - 14 || wx > BOX_X + BOX_W) return -1;

        int count;
        if (phase == Phase.ACT_MENU) count = ACTS.length;
        else if (phase == Phase.ITEM_MENU) count = inventoryItems.length;
        else if (phase == Phase.MERCY_MENU) count = MERCY_OPTS.length;
        else return -1;

        float lineH = 22f;
        for (int i = 0; i < count; i++) {
            float iy = ty - i * lineH;
            if (wy >= iy - 16 && wy <= iy + 6) {
                return i;
            }
        }
        return -1;
    }

    private void onMenuSelect() {
        switch (MENU_LABELS[menuIdx]) {
            case "FIGHT":
                float dmg = config.getPlayerAttackDamage() + (float)(Math.random() * 15f);
                enemyHp = Math.max(0, enemyHp - dmg);
                if (enemyHp <= 0) enemyDimmed = true;
                dialog = "You attacked for " + (int) dmg + " damage!";
                afterDialog = enemyHp <= 0 ? Phase.VICTORY : Phase.ENEMY_TURN;
                shakeTimer = 0.3f;
                showDamageSprite = true;
                damageShowTimer = 1.0f;
                fightAnimTimer = 1.2f;
                phase = Phase.FIGHT_ANIM;
                break;
            case "ACT":
                phase = Phase.ACT_MENU;
                menuIdx = 0;
                break;
            case "ITEM":
                if (inventoryItems.length == 0) {
                    dialog = "You have no items...";
                    afterDialog = Phase.ENEMY_TURN;
                    phase = Phase.DIALOG;
                    dialogCharIdx = 0;
                    dialogTimer = 0;
                } else {
                    phase = Phase.ITEM_MENU;
                    menuIdx = 0;
                }
                break;
            case "MERCY":
                phase = Phase.MERCY_MENU;
                menuIdx = 0;
                break;
        }
    }

    private void onActSelect() {
        afterDialog = Phase.ENEMY_TURN;
        switch (ACTS[menuIdx]) {
            case "Check":
                dialog = config.getDisplayName() + " - HP " + (int) enemyHp + "/" + (int) enemyMaxHp;
                break;
            case "Compliment":
                mercyLevel = Math.min(config.getMercyThreshold() + 1, mercyLevel + 1);
                dialog = "You complimented " + config.getDisplayName() + ".";
                break;
            case "Talk":
                mercyLevel = Math.min(config.getMercyThreshold() + 1, mercyLevel + 1);
                dialog = "You tried to talk. " + config.getDisplayName() + " seems interested.";
                break;
        }
        phase = Phase.DIALOG;
        dialogCharIdx = 0;
        dialogTimer = 0;
    }

    private void onMercySelect() {
        if (menuIdx == 0) {
            if (mercyLevel >= config.getMercyThreshold()) {
                enemyDimmed = true;
                dialog = "You spared " + config.getDisplayName() + ".";
                afterDialog = Phase.VICTORY;
                phase = Phase.DIALOG;
                dialogCharIdx = 0;
                dialogTimer = 0;
                return;
            } else {
                dialog = config.getDisplayName() + " doesn't want to be spared yet...";
                afterDialog = Phase.ENEMY_TURN;
            }
        } else {
            dialog = "You can't flee from this battle!";
            afterDialog = Phase.ENEMY_TURN;
        }
        phase = Phase.DIALOG;
        dialogCharIdx = 0;
        dialogTimer = 0;
    }

    private void onItemSelect() {
        if (menuIdx < 0 || menuIdx >= inventoryItems.length) return;
        Item item = inventoryItems[menuIdx];
        if (item != null && item.isEat()) {
            int heal = item.getPlusHP();
            if (heal > 0) {
                playerHp = Math.min(playerMaxHp, playerHp + heal);
            }
            Item[] newItems = new Item[inventoryItems.length - 1];
            System.arraycopy(inventoryItems, 0, newItems, 0, menuIdx);
            System.arraycopy(inventoryItems, menuIdx + 1, newItems, menuIdx, inventoryItems.length - menuIdx - 1);
            inventoryItems = newItems;
            if (menuIdx >= inventoryItems.length) menuIdx = Math.max(0, inventoryItems.length - 1);
            consumedItemIds.add(item.getId());
            dialog = "You ate " + item.getName() + "! HP +" + heal;
            afterDialog = Phase.ENEMY_TURN;
        } else {
            dialog = item != null ? item.getName() + " - Can't use in battle." : "Nothing here.";
            afterDialog = Phase.ENEMY_TURN;
        }
        phase = Phase.DIALOG;
        dialogCharIdx = 0;
        dialogTimer = 0;
    }

    private void startEnemyTurn() {
        int idx = (int)(Math.random() * allPatterns.length);
        currentAttack = allPatterns[idx];
        currentAttack.reset();
        projectiles.clear();
        attackTimer = 0;
        if (turnCount > 0) {
            heartX = BOX_X + BOX_W / 2f;
            heartY = BOX_Y + BOX_H / 2f;
        }
        turnCount++;
        phase = Phase.ENEMY_TURN;
    }

    private void endWithResult(CombatResult result) {
        Gdx.app.log("CombatScreen", "endWithResult: " + result + " playerHp=" + playerHp);
        if (prevMusicAsset != null) {
            game.getAudioService().playMusic(prevMusicAsset);
        }
        game.setPendingCombatResult(result);
        game.setPendingCombatPlayerHp(playerHp);
        game.setScreen(GameScreen.class);
    }


    @Override
    public void render(float delta) {
        delta = Math.min(delta, 1f / 30f);
        update(delta);
        draw();
    }

    private void update(float dt) {
        liquidTimer += dt;
        if (shakeTimer > 0) shakeTimer = Math.max(0, shakeTimer - dt);
        if (damageShowTimer > 0) {
            damageShowTimer -= dt;
            if (damageShowTimer <= 0) showDamageSprite = false;
        }

        switch (phase) {
            case DIALOG:
                dialogTimer += dt;
                if (dialogTimer > 0.03f) {
                    dialogTimer = 0;
                    if (dialogCharIdx < dialog.length()) dialogCharIdx++;
                }
                break;
            case FIGHT_ANIM:
                fightAnimTimer -= dt;
                if (fightAnimTimer <= 0) {
                    if (enemyHp <= 0) { phase = Phase.VICTORY; }
                    else { phase = Phase.DIALOG; dialogCharIdx = 0; dialogTimer = 0; }
                }
                break;
            case ENEMY_TURN:
                updateEnemyTurn(dt);
                break;
            case VICTORY:
            case DEFEAT:
                break;
            default: break;
        }
    }

    private void updateEnemyTurn(float dt) {
        float dx = 0, dy = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) dx -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) dx += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W)) dy += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)) dy -= 1;
        if (dx != 0 && dy != 0) { dx *= 0.707f; dy *= 0.707f; }
        heartX += dx * HEART_SPEED * dt * 60f;
        heartY += dy * HEART_SPEED * dt * 60f;
        float half = HEART_SIZE / 2f;
        heartX = Math.max(BOX_X + half + 2, Math.min(BOX_X + BOX_W - half - 2, heartX));
        heartY = Math.max(BOX_Y + half + 2, Math.min(BOX_Y + BOX_H - half - 2, heartY));

        attackTimer += dt;
        if (currentAttack != null) {
            currentAttack.spawn(attackTimer, dt, projectiles);
        }

        Iterator<CombatProjectile> it = projectiles.iterator();
        List<CombatProjectile> toAdd = new ArrayList<>();
        while (it.hasNext()) {
            CombatProjectile p = it.next();
            if (p.type == CombatProjectile.Type.WARNING) {
                p.life += dt;
                if (p.life >= p.maxLife) {
                    it.remove();
                    boolean vertical = p.h >= p.w;
                    CombatProjectile laser;
                    if (vertical) {
                        laser = new CombatProjectile(p.x - 4, p.y, 0, 0, p.w + 8, p.h, CombatProjectile.Type.LASER);
                    } else {
                        laser = new CombatProjectile(p.x, p.y - 4, 0, 0, p.w, p.h + 8, CombatProjectile.Type.LASER);
                    }
                    laser.life = 0;
                    laser.maxLife = 0.35f;
                    toAdd.add(laser);
                }
            } else if (p.type == CombatProjectile.Type.LASER) {
                p.life += dt;
                if (p.life >= p.maxLife) it.remove();
            } else if (p.type == CombatProjectile.Type.FORK) {
                if (p.life < 0f) {
                    p.life += dt;
                    if (p.life >= 0f) {
                        dx = heartX - p.x;
                        dy = heartY - p.y;
                        float len = (float)Math.sqrt(dx * dx + dy * dy);
                        if (len < 1f) len = 1f;
                        float speed = p.maxLife > 0 ? p.maxLife : 240f;
                        p.vx = dx / len * speed;
                        p.vy = dy / len * speed;
                    }
                }
                p.x += p.vx * dt;
                p.y += p.vy * dt;
                if (p.x < BOX_X - 60 || p.x > BOX_X + BOX_W + 60 ||
                    p.y < BOX_Y - 60 || p.y > BOX_Y + BOX_H + 60) {
                    it.remove();
                }
            } else {
                p.x += p.vx * dt;
                p.y += p.vy * dt;
                if (p.x < BOX_X - 60 || p.x > BOX_X + BOX_W + 60 ||
                    p.y < BOX_Y - 60 || p.y > BOX_Y + BOX_H + 60) {
                    it.remove();
                }
            }
        }
        projectiles.addAll(toAdd);

        if (iFrameTimer > 0) {
            iFrameTimer -= dt;
        } else {
            for (CombatProjectile p : projectiles) {
                if (p.type == CombatProjectile.Type.WARNING) continue;
                if (hitsHeart(p)) {
                    float dmg = (p.type == CombatProjectile.Type.LASER) ? config.getProjectileDamage() + 1 : config.getProjectileDamage();
                    playerHp = Math.max(0, playerHp - dmg);
                    iFrameTimer = IFRAME_DURATION;
                    shakeTimer = 0.2f;
                    game.getAudioService().playSound(com.github.MichalKC.manylands.asset.SoundAsset.COMBAT_HIT);
                    if (playerHp <= 0) { phase = Phase.DEFEAT; return; }
                    break;
                }
            }
        }

        if (currentAttack != null && attackTimer >= currentAttack.getDuration()) {
            projectiles.clear();
            attackIndex++;
            String[] lines = config.getTurnLines();
            dialog = lines[turnCount % lines.length];
            afterDialog = Phase.MENU;
            phase = Phase.DIALOG;
            dialogCharIdx = 0;
            dialogTimer = 0;
        }
    }

    private boolean hitsHeart(CombatProjectile p) {
        float s = HEART_HITBOX_SHRINK;
        float mx = heartX - HEART_SIZE / 2f + s;
        float my = heartY - HEART_SIZE / 2f + s;
        float mw = HEART_SIZE - s * 2;
        float mh = HEART_SIZE - s * 2;

        float px = p.x, py = p.y, pw = p.w, ph = p.h;
        if (p.type == CombatProjectile.Type.KNIFE || p.type == CombatProjectile.Type.BONE) {
            float minorShrink = 4f;
            float majorTrim   = 6f;
            if (p.type == CombatProjectile.Type.BONE && ph > pw) {
                float targetW = 6f;
                if (pw > targetW) {
                    float dw = pw - targetW;
                    px += dw / 2f; pw = targetW;
                }
                float dhMajor = Math.min(ph - 1f, majorTrim);
                py += dhMajor / 2f; ph -= dhMajor;
            } else if (ph <= pw) {
                float dh = Math.min(ph - 1f, minorShrink);
                py += dh / 2f; ph -= dh;
                float dwMajor = Math.min(pw - 1f, majorTrim);
                px += dwMajor / 2f; pw -= dwMajor;
            } else {
                float dw = Math.min(pw - 1f, minorShrink);
                px += dw / 2f; pw -= dw;
                float dhMajor = Math.min(ph - 1f, majorTrim);
                py += dhMajor / 2f; ph -= dhMajor;
            }
        }

        return mx < px + pw && mx + mw > px && my < py + ph && my + mh > py;
    }


    private void draw() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply();

        float sx = 0, sy = 0;
        if (shakeTimer > 0) {
            sx = (float)(Math.random() - 0.5) * shakeTimer * 30f;
            sy = (float)(Math.random() - 0.5) * shakeTimer * 30f;
        }

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        drawEnemy(sx, sy);
        drawEnemyNameAndHp(sx, sy);
        batch.end();

        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);

        if (phase == Phase.ENEMY_TURN) {
            drawBattleBox(sx, sy);
            drawProjectiles(sx, sy);
            drawHeart(sx, sy);
        } else {
            drawBattleBox(sx, sy);
            batch.begin();
            drawBoxContent(sx, sy);
            batch.end();
        }

        batch.begin();
        drawBottomUI(sx, sy);
        batch.end();
    }

    private boolean isSpared() { return mercyLevel >= config.getMercyThreshold(); }

    private void drawEnemy(float sx, float sy) {
        Texture tex = (showDamageSprite && enemyDamageTexture != null) ? enemyDamageTexture : enemyTexture;
        if (tex == null) return;
        float maxSz = ENEMY_MAX_SIZE;
        float scale = Math.min(maxSz / tex.getWidth(), maxSz / tex.getHeight());
        float w = tex.getWidth() * scale;
        float h = tex.getHeight() * scale;
        if (enemyDimmed) batch.setColor(0.35f, 0.35f, 0.35f, 1f);
        batch.draw(tex, ENEMY_CENTER_X - w / 2f + sx, ENEMY_CENTER_Y - h / 2f + sy, w, h);
        if (enemyDimmed) batch.setColor(Color.WHITE);
    }

    private void drawEnemyNameAndHp(float sx, float sy) {
        font.setColor(Color.WHITE);
        layout.setText(font, config.getDisplayName());
        font.draw(batch, config.getDisplayName(), CANVAS_W / 2f - layout.width / 2f + sx, ENEMY_NAME_Y + sy);
    }

    private void drawBattleBox(float sx, float sy) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        Gdx.gl.glLineWidth(3f);
        shapeRenderer.rect(BOX_X + sx, BOX_Y + sy, BOX_W, BOX_H);
        shapeRenderer.end();
    }

    private void drawProjectiles(float sx, float sy) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (CombatProjectile p : projectiles) {
            float px = p.x + sx, py = p.y + sy;
            if (px + p.w < BOX_X + sx || px > BOX_X + BOX_W + sx) continue;
            if (py + p.h < BOX_Y + sy || py > BOX_Y + BOX_H + sy) continue;

            shapeRenderer.setColor(Color.WHITE);
            switch (p.type) {
                case BONE:
                    drawPixelKatana(px, py, p.w, p.h);
                    break;
                case PELLET:
                    drawPixelShuriken(px + p.w / 2f, py + p.h / 2f, p.w / 2f);
                    break;
                case HEART:
                    drawPixelBomb(px + p.w / 2f, py + p.h / 2f, p.w / 2f);
                    break;
                case SPOON:
                    drawPixelSpoon(px, py, p.w, p.h);
                    break;
                case POT:
                    drawPixelPot(px, py, p.w, p.h);
                    break;
                case KNIFE:
                    drawPixelKnife(px, py, p.w, p.h);
                    break;
                case FORK:
                    drawPixelFork(px, py, p.w, p.h, p.vx, p.vy);
                    break;
                case WARNING: {
                    boolean flash = ((int)(p.life * 10f)) % 2 == 0;
                    shapeRenderer.setColor(flash ? new Color(1f, 1f, 0f, 0.5f) : new Color(1f, 1f, 0f, 0.15f));
                    shapeRenderer.rect(px, py, p.w, p.h);
                    shapeRenderer.setColor(Color.WHITE);
                    break;
                }
                case LASER:
                    drawPixelCatLaser(px, py, p.w, p.h);
                    break;
            }
        }

        boolean blink = iFrameTimer > 0 && ((int)(iFrameTimer * 15f)) % 2 == 0;
        if (!blink) {
            shapeRenderer.setColor(new Color(0.4f, 0.85f, 0.5f, 0.75f));
            float hx = heartX + sx, hy = heartY + sy;
            float r = HEART_SIZE / 2f;
            shapeRenderer.circle(hx - r * 0.4f, hy + r * 0.2f, r * 0.5f);
            shapeRenderer.circle(hx + r * 0.4f, hy + r * 0.2f, r * 0.5f);
            shapeRenderer.triangle(hx - r * 0.85f, hy, hx + r * 0.85f, hy, hx, hy - r * 1.0f);
        }
        shapeRenderer.end();
    }

    private void drawHeart(float sx, float sy) {
    }

    private void drawBoxContent(float sx, float sy) {
        font.setColor(Color.WHITE);
        float pad = 14f;
        float tx = BOX_X + pad + sx;
        float ty = BOX_Y + BOX_H - pad + sy;
        float wrapW = BOX_W - pad * 2;

        switch (phase) {
            case START:
                font.setColor(Color.YELLOW);
                layout.setText(font, "COMBAT BEGINS");
                font.draw(batch, "COMBAT BEGINS", BOX_X + (BOX_W - layout.width) / 2f + sx, ty);
                font.setColor(Color.WHITE);
                font.draw(batch, "Press SPACE to start", tx, ty - 26);
                font.setColor(Color.RED);
                layout.setText(font, "Exit [ESC] = DEATH", Color.RED, wrapW, com.badlogic.gdx.utils.Align.left, true);
                font.draw(batch, layout, tx, ty - 50);
                break;
            case DIALOG:
                String text = dialog.substring(0, Math.min(dialogCharIdx, dialog.length()));
                layout.setText(font, text, Color.WHITE, wrapW, com.badlogic.gdx.utils.Align.left, true);
                font.draw(batch, layout, tx, ty);
                break;
            case MENU:
                font.setColor(new Color(0.5f, 0.5f, 0.5f, 1f));
                layout.setText(font, "* What will you do?", font.getColor(), wrapW, com.badlogic.gdx.utils.Align.left, true);
                font.draw(batch, layout, tx, ty);
                break;
            case ACT_MENU:
                for (int i = 0; i < ACTS.length; i++) {
                    float iy = ty - i * 22;
                    if (i == menuIdx) {
                        font.setColor(Color.RED);
                        font.draw(batch, "> ", tx - 14, iy);
                    }
                    font.setColor(Color.WHITE);
                    font.draw(batch, "* " + ACTS[i], tx, iy);
                }
                break;
            case ITEM_MENU:
                int maxVisible = (int)((BOX_H - pad * 2) / 22);
                int itemStart = Math.max(0, menuIdx - maxVisible + 1);
                int itemEnd = Math.min(inventoryItems.length, itemStart + maxVisible);
                for (int i = itemStart; i < itemEnd; i++) {
                    float iy = ty - (i - itemStart) * 22;
                    if (i == menuIdx) {
                        font.setColor(Color.YELLOW);
                        font.draw(batch, "> ", tx - 14, iy);
                    }
                    Item it = inventoryItems[i];
                    boolean eatable = it != null && it.isEat();
                    font.setColor(eatable ? new Color(0.2f, 1f, 0.3f, 1f) : Color.WHITE);
                    String label = it != null ? it.getName() : "???";
                    if (eatable) label += "  [HP+" + it.getPlusHP() + "]";
                    font.draw(batch, "* " + label, tx, iy);
                }
                break;
            case MERCY_MENU:
                for (int i = 0; i < MERCY_OPTS.length; i++) {
                    float iy = ty - i * 22;
                    if (i == menuIdx) {
                        font.setColor(Color.RED);
                        font.draw(batch, "> ", tx - 14, iy);
                    }
                    boolean spareYellow = i == 0 && isSpared();
                    font.setColor(spareYellow ? Color.YELLOW : Color.WHITE);
                    font.draw(batch, "* " + MERCY_OPTS[i], tx, iy);
                }
                break;
            case FIGHT_ANIM:
                font.setColor(Color.WHITE);
                layout.setText(font, "ATTACK!");
                font.draw(batch, "ATTACK!", BOX_X + (BOX_W - layout.width) / 2f + sx, BOX_Y + BOX_H / 2f + layout.height / 2f + sy);
                break;
            case VICTORY:
                font.setColor(Color.YELLOW);
                layout.setText(font, "YOU WON!");
                font.draw(batch, "YOU WON!", BOX_X + (BOX_W - layout.width) / 2f + sx, BOX_Y + BOX_H / 2f + 14 + sy);
                font.setColor(Color.WHITE);
                font.draw(batch, "Press SPACE to continue", tx, BOX_Y + BOX_H / 2f - 10 + sy);
                break;
            case DEFEAT:
                font.setColor(Color.RED);
                layout.setText(font, "GAME OVER");
                font.draw(batch, "GAME OVER", BOX_X + (BOX_W - layout.width) / 2f + sx, BOX_Y + BOX_H / 2f + 14 + sy);
                font.setColor(Color.WHITE);
                font.draw(batch, "Press SPACE to continue", tx, BOX_Y + BOX_H / 2f - 10 + sy);
                break;
            default: break;
        }
    }

    private void drawBottomUI(float sx, float sy) {
        float hpY = PLAYER_HP_Y + sy;
        font.setColor(Color.WHITE);
        font.draw(batch, "HP", BOX_X + sx, hpY + 14);

        batch.end();

        float phpX = BOX_X + 35 + sx;
        float hpFrac = playerMaxHp > 0 ? playerHp / playerMaxHp : 0;
        drawLiquidBar(phpX, hpY, 120, 14, hpFrac);

        float ehpFrac = enemyMaxHp > 0 ? enemyHp / enemyMaxHp : 0;
        drawLiquidBar(ENEMY_HP_BAR_X + sx, ENEMY_HP_BAR_Y + sy, ENEMY_HP_BAR_W, ENEMY_HP_BAR_H, ehpFrac);

        batch.begin();
        font.setColor(Color.WHITE);
        font.draw(batch, (int) playerHp + " / " + (int) playerMaxHp, phpX + 130 + sx, hpY + 14);

        if (phase == Phase.MENU || phase == Phase.ACT_MENU || phase == Phase.ITEM_MENU || phase == Phase.MERCY_MENU) {
            float btnW = MENU_BTN_W, btnH = MENU_BTN_H;
            float totalW = MENU_LABELS.length * btnW + (MENU_LABELS.length - 1) * 12;
            float startX = (CANVAS_W - totalW) / 2f;
            float by = MENU_BTN_Y + sy;
            for (int i = 0; i < MENU_LABELS.length; i++) {
                float bx = startX + i * (btnW + 12) + sx;
                boolean active = (phase == Phase.MENU && i == menuIdx)
                    || (phase == Phase.ACT_MENU && i == 1)
                    || (phase == Phase.ITEM_MENU && i == 2)
                    || (phase == Phase.MERCY_MENU && i == 3);
                batch.end();
                drawPixelButton(bx, by, btnW, btnH, active);
                batch.begin();
                font.setColor(active ? Color.BLACK : new Color(0.55f, 0.55f, 0.55f, 1f));
                layout.setText(font, MENU_LABELS[i]);
                font.draw(batch, MENU_LABELS[i], bx + (btnW - layout.width) / 2f, by + btnH / 2f + layout.height / 2f);
            }
        }
    }

    private void drawLiquidBar(float x, float y, float w, float h, float frac) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        float fillW = w * frac;
        int segments = Math.max(1, (int)(fillW / 3f));
        float segW = fillW / segments;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(new Color(0.1f, 0.1f, 0.1f, 1f));
        shapeRenderer.rect(x, y, w, h);
        shapeRenderer.setColor(new Color(0.9f, 0.9f, 0.9f, 1f));
        for (int i = 0; i < segments; i++) {
            float sx2 = x + i * segW;
            float dripPhase = liquidTimer * 2.5f + i * 0.7f;
            float dripH = (float)(Math.sin(dripPhase) * 0.3f + 0.7f) * h;
            float topBump = (float)(Math.sin(dripPhase * 1.3f + 0.5f)) * 2f;
            shapeRenderer.rect(sx2, y, segW + 0.5f, dripH + topBump);
        }

        for (int i = 0; i < 3; i++) {
            float dripPhase = liquidTimer * 1.8f + i * 2.1f + frac * 5f;
            float dripProgress = (dripPhase % 3f) / 3f;
            if (dripProgress < 1f && frac > 0.05f) {
                float dropX = x + (fillW * (0.2f + 0.6f * ((i * 0.37f + frac) % 1f)));
                float dropY = y - dripProgress * 8f;
                float dropAlpha = 1f - dripProgress;
                shapeRenderer.setColor(new Color(0.9f, 0.9f, 0.9f, dropAlpha));
                shapeRenderer.circle(dropX, dropY, 1.5f);
            }
        }
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(2f);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(x, y, w, h);
        if (frac > 0.01f && frac < 0.99f) {
            float lx = x + fillW;
            shapeRenderer.line(lx, y, lx, y + h);
        }
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void px(float x, float y) { shapeRenderer.rect(x, y, 1, 1); }
    private void px2(float x, float y) { shapeRenderer.rect(x, y, 2, 2); }

    private void drawPixelButton(float x, float y, float w, float h, boolean active) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(active ? Color.WHITE : new Color(0.07f, 0.07f, 0.07f, 1f));
        shapeRenderer.rect(x, y, w, h);
        Color border = active ? Color.BLACK : new Color(0.55f, 0.55f, 0.55f, 1f);
        shapeRenderer.setColor(border);
        for (float i = x + 2; i < x + w - 2; i++) shapeRenderer.rect(i, y + h - 1, 1, 1);
        for (float i = x + 2; i < x + w - 2; i++) shapeRenderer.rect(i, y, 1, 1);
        for (float j = y + 2; j < y + h - 2; j++) shapeRenderer.rect(x, j, 1, 1);
        for (float j = y + 2; j < y + h - 2; j++) shapeRenderer.rect(x + w - 1, j, 1, 1);
        shapeRenderer.rect(x + 1, y + h - 2, 1, 1);
        shapeRenderer.rect(x + 1, y + 1, 1, 1);
        shapeRenderer.rect(x + w - 2, y + h - 2, 1, 1);
        shapeRenderer.rect(x + w - 2, y + 1, 1, 1);
        if (active) {
            for (float i = x + 2; i < x + w - 2; i++) shapeRenderer.rect(i, y + h - 2, 1, 1);
            for (float i = x + 2; i < x + w - 2; i++) shapeRenderer.rect(i, y + 1, 1, 1);
            for (float j = y + 2; j < y + h - 2; j++) shapeRenderer.rect(x + 1, j, 1, 1);
            for (float j = y + 2; j < y + h - 2; j++) shapeRenderer.rect(x + w - 2, j, 1, 1);
        }
        shapeRenderer.end();
    }

    private void drawPixelKatana(float ox, float oy, float w, float h) {
        boolean vert = h > w;
        float ps = 2f;
        shapeRenderer.setColor(Color.WHITE);
        if (vert) {
            float bx = ox + w / 2f - ps;
            float by = oy;
            shapeRenderer.rect(bx, by, ps, ps * 2);
            shapeRenderer.rect(bx - ps, by + ps * 2, ps * 4, ps);
            for (int r = 0; r < 8; r++) {
                float bw = (r < 6) ? ps * 2 : ps;
                shapeRenderer.rect(bx + (r >= 6 ? ps * 0.5f : 0), by + ps * 3 + r * ps, bw, ps);
            }
            shapeRenderer.rect(bx + ps * 0.5f, by + ps * 11, ps, ps);
        } else {
            float bx = ox;
            float by = oy + h / 2f - ps;
            shapeRenderer.rect(bx, by, ps * 2, ps);
            shapeRenderer.rect(bx + ps * 2, by - ps, ps, ps * 3);
            for (int c = 0; c < 8; c++) {
                float bh = (c < 6) ? ps : ps * 0.5f;
                shapeRenderer.rect(bx + ps * 3 + c * ps, by + (c >= 6 ? ps * 0.25f : 0), ps, (c < 6) ? ps : ps * 0.5f);
            }
            shapeRenderer.rect(bx + ps * 11, by + ps * 0.25f, ps, ps * 0.5f);
        }
    }

    private void drawPixelShuriken(float cx, float cy, float r) {
        float ps = Math.max(1f, r / 5f);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(cx - ps, cy - ps, ps * 2, ps * 2);
        shapeRenderer.rect(cx - ps, cy + ps, ps * 2, ps);
        shapeRenderer.rect(cx - ps * 0.5f, cy + ps * 2, ps, ps);
        shapeRenderer.rect(cx - ps * 0.5f, cy + ps * 3, ps, ps);
        shapeRenderer.rect(cx - ps, cy - ps * 2, ps * 2, ps);
        shapeRenderer.rect(cx - ps * 0.5f, cy - ps * 3, ps, ps);
        shapeRenderer.rect(cx - ps * 0.5f, cy - ps * 4, ps, ps);
        shapeRenderer.rect(cx + ps, cy - ps, ps, ps * 2);
        shapeRenderer.rect(cx + ps * 2, cy - ps * 0.5f, ps, ps);
        shapeRenderer.rect(cx + ps * 3, cy - ps * 0.5f, ps, ps);
        shapeRenderer.rect(cx - ps * 2, cy - ps, ps, ps * 2);
        shapeRenderer.rect(cx - ps * 3, cy - ps * 0.5f, ps, ps);
        shapeRenderer.rect(cx - ps * 4, cy - ps * 0.5f, ps, ps);
        shapeRenderer.rect(cx + ps, cy + ps, ps, ps);
        shapeRenderer.rect(cx - ps * 2, cy + ps, ps, ps);
        shapeRenderer.rect(cx + ps, cy - ps * 2, ps, ps);
        shapeRenderer.rect(cx - ps * 2, cy - ps * 2, ps, ps);
    }

    private void drawPixelBomb(float cx, float cy, float r) {
        float ps = Math.max(1f, r / 5f);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(cx - ps, cy + ps * 3, ps * 2, ps);
        shapeRenderer.rect(cx - ps * 2, cy + ps * 2, ps * 4, ps);
        shapeRenderer.rect(cx - ps * 3, cy + ps, ps * 6, ps);
        shapeRenderer.rect(cx - ps * 3, cy, ps * 6, ps);
        shapeRenderer.rect(cx - ps * 3, cy - ps, ps * 6, ps);
        shapeRenderer.rect(cx - ps * 2, cy - ps * 2, ps * 4, ps);
        shapeRenderer.rect(cx - ps, cy - ps * 3, ps * 2, ps);
        shapeRenderer.rect(cx + ps, cy + ps * 4, ps, ps);
        shapeRenderer.rect(cx + ps * 2, cy + ps * 5, ps, ps);
        shapeRenderer.rect(cx + ps, cy + ps * 6, ps, ps);
        shapeRenderer.rect(cx + ps * 2, cy + ps * 7, ps, ps);
        shapeRenderer.rect(cx + ps * 3, cy + ps * 7, ps, ps);
        shapeRenderer.rect(cx + ps * 2, cy + ps * 8, ps, ps);
    }

    private void drawPixelCatLaser(float x, float y, float w, float h) {
        boolean horiz = w > h;
        float extra = 3f;

        shapeRenderer.setColor(Color.WHITE);
        if (horiz) {
            float drawX = Math.max(CombatConstants.BOX_X, x - extra);
            float drawW = Math.min(CombatConstants.BOX_X + CombatConstants.BOX_W - drawX, w + extra * 2f);
            shapeRenderer.rect(drawX, y, drawW, h);
        } else {
            float drawY = Math.max(CombatConstants.BOX_Y, y - extra);
            float drawH = Math.min(CombatConstants.BOX_Y + CombatConstants.BOX_H - drawY, h + extra * 2f);
            shapeRenderer.rect(x, drawY, w, drawH);
        }
    }

    private void drawPixelSpoon(float ox, float oy, float w, float h) {
        boolean vert = h >= w;
        float ps = Math.max(1f, Math.min(w, h) / 8f);
        if (vert) {
            float cx = ox + w / 2f;
            for (int i = 0; i < 7; i++) shapeRenderer.rect(cx - ps * 0.5f, oy + i * ps, ps, ps * 0.9f);
            float by = oy + ps * 5.0f;
            shapeRenderer.rect(cx - ps * 1.5f, by, ps * 3f, ps);
            shapeRenderer.rect(cx - ps * 2.0f, by + ps, ps * 4f, ps);
            shapeRenderer.rect(cx - ps * 2.0f, by + ps * 2f, ps * 4f, ps);
            shapeRenderer.rect(cx - ps * 1.5f, by + ps * 3f, ps * 3f, ps);
            shapeRenderer.rect(cx - ps, by + ps * 4f, ps * 2f, ps * 0.8f);
        } else {
            float cy = oy + h / 2f;
            for (int i = 0; i < 7; i++) shapeRenderer.rect(ox + i * ps, cy - ps * 0.5f, ps * 0.9f, ps);
            float bx = ox + ps * 5.0f;
            shapeRenderer.rect(bx, cy - ps * 1.5f, ps, ps * 3f);
            shapeRenderer.rect(bx + ps, cy - ps * 2f, ps, ps * 4f);
            shapeRenderer.rect(bx + ps * 2f, cy - ps * 2f, ps, ps * 4f);
            shapeRenderer.rect(bx + ps * 3f, cy - ps * 1.5f, ps, ps * 3f);
            shapeRenderer.rect(bx + ps * 4f, cy - ps, ps * 0.8f, ps * 2f);
        }
    }

    private void drawPixelPot(float ox, float oy, float w, float h) {
        float ps = Math.max(1f, Math.min(w, h) / 12f);
        shapeRenderer.rect(ox + ps, oy, w - ps * 2, h - ps * 2);
        shapeRenderer.rect(ox, oy + h - ps * 3, w, ps);
        shapeRenderer.rect(ox + ps * 0.5f, oy + h - ps * 2, w - ps, ps * 0.7f);
        shapeRenderer.rect(ox + ps * 2, oy + h - ps * 1.3f, w - ps * 4, ps * 0.6f);
        shapeRenderer.rect(ox + w / 2f - ps * 0.7f, oy + h - ps * 0.7f, ps * 1.4f, ps * 0.7f);
        for (int i = 1; i <= 3; i++) {
            float sx = ox + ps * (1 + i * 0.8f);
            shapeRenderer.rect(sx, oy + ps * 1.0f, ps * 0.5f, h - ps * 4f);
        }
        shapeRenderer.rect(ox - ps * 0.8f, oy + h / 2f - ps * 1.0f, ps * 0.8f, ps * 2.0f);
        shapeRenderer.rect(ox + w, oy + h / 2f - ps * 1.0f, ps * 0.8f, ps * 2.0f);
    }

    private void drawPixelKnife(float ox, float oy, float w, float h) {
        boolean horiz = w >= h;
        float ps = Math.max(1f, Math.min(w, h) / 7f);
        if (horiz) {
            float cy = oy + h / 2f;
            shapeRenderer.rect(ox, cy - ps, ps * 2.0f, ps * 2.2f);
            shapeRenderer.rect(ox + ps * 2.0f, cy - ps * 0.6f, ps * 1.0f, ps * 1.2f);
            float bx = ox + ps * 3.2f;
            float bw = Math.max(ps * 4.0f, w - ps * 4.8f);
            for (int i = 0; i < (int)(bw / ps); i++) {
                float step = i * ps;
                float th = ps * (0.9f - 0.15f * (step / Math.max(ps, bw)));
                shapeRenderer.rect(bx + step, cy - th / 2f, ps, th);
            }
            shapeRenderer.rect(bx + bw, cy - ps * 0.25f, ps * 0.9f, ps * 0.5f);
        } else {
            float cx = ox + w / 2f;
            shapeRenderer.rect(cx - ps, oy, ps * 2.2f, ps * 2.0f);
            shapeRenderer.rect(cx - ps * 0.6f, oy + ps * 1.8f, ps * 1.2f, ps * 1.0f);
            float by = oy + ps * 3.2f;
            float bh = Math.max(ps * 4.0f, h - ps * 4.8f);
            for (int i = 0; i < (int)(bh / ps); i++) {
                float step = i * ps;
                float tw = ps * (0.9f - 0.15f * (step / Math.max(ps, bh)));
                shapeRenderer.rect(cx - tw / 2f, by + step, tw, ps);
            }
            shapeRenderer.rect(cx - ps * 0.2f, by + bh, ps * 0.4f, ps * 1.0f);
        }
    }

    private void drawPixelFork(float ox, float oy, float w, float h, float vx, float vy) {
        boolean horiz = w >= h;
        float ps = Math.max(1f, Math.min(w, h) / 7f);
        if (horiz) {
            float cy = oy + h / 2f;
            shapeRenderer.rect(ox, cy - ps * 0.6f, ps * 3.2f, ps * 1.2f);
            float hx = ox + ps * 3.2f;
            shapeRenderer.rect(hx, cy - ps * 1.1f, ps * 1.3f, ps * 2.2f);
            float t0 = hx + ps * 1.4f;
            shapeRenderer.rect(t0, cy + ps * 0.6f, ps * 1.2f, ps * 0.5f);
            shapeRenderer.rect(t0, cy - ps * 0.1f, ps * 1.4f, ps * 0.5f);
            shapeRenderer.rect(t0, cy - ps * 0.8f, ps * 1.2f, ps * 0.5f);
            shapeRenderer.rect(t0, cy - ps * 1.5f, ps * 1.0f, ps * 0.5f);
        } else {
            float cx = ox + w / 2f;
            boolean faceDown = vy < 0f;
            if (!faceDown) {
                shapeRenderer.rect(cx - ps * 0.6f, oy, ps * 1.2f, ps * 3.2f);
                float hy = oy + ps * 3.2f;
                shapeRenderer.rect(cx - ps, hy, ps * 2.2f, ps * 1.3f);
                shapeRenderer.rect(cx - ps * 1.5f, hy + ps * 1.2f, ps * 0.6f, ps * 1.4f);
                shapeRenderer.rect(cx - ps * 0.4f, hy + ps * 1.2f, ps * 0.6f, ps * 1.7f);
                shapeRenderer.rect(cx + ps * 0.7f, hy + ps * 1.2f, ps * 0.6f, ps * 1.4f);
                shapeRenderer.rect(cx + ps * 1.8f, hy + ps * 1.2f, ps * 0.6f, ps * 1.2f);
            } else {
                shapeRenderer.rect(cx - ps * 0.6f, oy + h - ps * 3.2f, ps * 1.2f, ps * 3.2f);
                float hy = oy + h - ps * 3.2f - ps * 1.3f;
                shapeRenderer.rect(cx - ps, hy, ps * 2.2f, ps * 1.3f);
                shapeRenderer.rect(cx - ps * 1.5f, hy - ps * 1.4f, ps * 0.6f, ps * 1.4f);
                shapeRenderer.rect(cx - ps * 0.4f, hy - ps * 1.7f, ps * 0.6f, ps * 1.7f);
                shapeRenderer.rect(cx + ps * 0.7f, hy - ps * 1.4f, ps * 0.6f, ps * 1.4f);
                shapeRenderer.rect(cx + ps * 1.8f, hy - ps * 1.2f, ps * 0.6f, ps * 1.2f);
            }
        }
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
        if (enemyTexture != null) enemyTexture.dispose();
        if (enemyDamageTexture != null) enemyDamageTexture.dispose();
    }
}
