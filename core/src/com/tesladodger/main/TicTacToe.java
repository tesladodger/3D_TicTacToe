package com.tesladodger.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import java.util.List;
import java.util.Vector;


public class TicTacToe extends InputAdapter implements ApplicationListener {

    // Player
    private int player = 0; // next player = 1 - player
    private List<Integer> player1List = new Vector();
    private List<Integer> player2List = new Vector();
    private List<Integer> listToCheck = new Vector();
    private List<Integer> inner = new Vector();
    private int score1 = 0, score2 = 0;
    private boolean gameWon = false;

    // Camera
    private PerspectiveCamera cam3d;
    private CameraInputController camController;

    // Cubes
    private static class CubeObject extends ModelInstance {
        private final Vector3 center = new Vector3();
        private final Vector3 dimensions = new Vector3();
        private final float radius;
        private final static BoundingBox bounds = new BoundingBox();
        private CubeObject(Model model) {
            super(model);
            calculateBoundingBox(bounds);
            bounds.getCenter(center);
            bounds.getDimensions(dimensions);
            radius = dimensions.len() / 2f;
        }
    }
    private List<Integer> selectedList = new Vector();
    private Vector3 position = new Vector3();
    private int selected = -1, selecting = -1;
    private boolean cubeIsUsed;
    private Material player1Material;  // blue cube
    private Material player2Material;  // red cube
    private Material originalMaterial; // white fish
    private ModelBatch modelBatch;
    private Model cube;
    private Array<CubeObject> cubeInstances = new Array<CubeObject>();
    private Environment environment;

    // Stage Stuff
    private Stage stage;
    private ImageButton playerButton1;
    private ImageButton playerButton2;
    private ImageButton gameWonButton1;
    private ImageButton gameWonButton2;
    private ImageButton drawButton;
    private Label scoreBoard;
    private StringBuilder scoreBoardBuilder;
    private Label debugLabel;
    private StringBuilder debugLabelBuilder;

    @Override
    public void create() {
        /* ______________________________STAGE_AND_BUTTONS__________________ */
        // Debug
        BitmapFont font   = new BitmapFont();
        debugLabel        = new Label("Send", new Label.LabelStyle(font, Color.WHITE));
        debugLabelBuilder = new StringBuilder();

        // Score Board
        scoreBoard = new Label("Nudes", new Label.LabelStyle(font, Color.WHITE));
        scoreBoard.setPosition(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() * .9f);
        scoreBoard.setFontScale(2f); // This looks like shit
        scoreBoardBuilder = new StringBuilder();

        // Player indicator
        Texture pBT                 = new Texture(Gdx.files.internal("player1.png"));
        TextureRegion pBTR          = new TextureRegion(pBT);
        TextureRegionDrawable pBTRD = new TextureRegionDrawable(pBTR);
        playerButton1               = new ImageButton(pBTRD);
        playerButton1.setSize(Gdx.graphics.getWidth() / 5f, Gdx.graphics.getHeight());
        pBT           = new Texture(Gdx.files.internal("player2.png"));
        pBTR          = new TextureRegion(pBT);
        pBTRD         = new TextureRegionDrawable(pBTR);
        playerButton2 = new ImageButton(pBTRD);
        playerButton2.setSize(Gdx.graphics.getWidth() / 5f, Gdx.graphics.getHeight());

        // Win and draw buttons
        Texture gWT                 = new Texture(Gdx.files.internal("win1.png"));
        TextureRegion gWTR          = new TextureRegion(gWT);
        TextureRegionDrawable gWTRD = new TextureRegionDrawable(gWTR);
        gameWonButton1              = new ImageButton(gWTRD);
        gameWonButton1.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        gameWonButton1.setPosition(0,0);
        gameWonButton1.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                resetGame();
            }
        });
        gWT            = new Texture(Gdx.files.internal("win2.png"));
        gWTR           = new TextureRegion(gWT);
        gWTRD          = new TextureRegionDrawable(gWTR);
        gameWonButton2 = new ImageButton(gWTRD);
        gameWonButton2.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        gameWonButton2.setPosition(0,0);
        gameWonButton2.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                resetGame();
            }
        });
        gWT        = new Texture(Gdx.files.internal("draw.png"));
        gWTR       = new TextureRegion(gWT);
        gWTRD      = new TextureRegionDrawable(gWTR);
        drawButton = new ImageButton(gWTRD);
        drawButton.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        drawButton.setPosition(0, 0);
        drawButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                resetGame();
            }
        });

        stage = new Stage();
        stage.addActor(scoreBoard);
        stage.addActor(debugLabel);

        /* ______________________________3D_STUFF___________________________ */
        modelBatch = new ModelBatch();

        // Camera
        cam3d = new PerspectiveCamera(
                67,
                Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight() );
        cam3d.position.set(10f, 14f, 22f);
        cam3d.lookAt(0,0,0);
        cam3d.near = 1f;
        cam3d.far  = 300f;
        cam3d.update();
        camController = new CameraInputController(cam3d);
        Gdx.input.setInputProcessor(new InputMultiplexer(this, camController));

        // Lights
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, 1f, 0.8f, 0.2f));

        // Cubes
        ModelBuilder modelBuilder = new ModelBuilder();
        player1Material  = new Material();
        player1Material.set(ColorAttribute.createDiffuse(0f, 0f, .7f, 1f));
        player2Material  = new Material();
        player2Material.set(ColorAttribute.createDiffuse(.7f, 0f, 0f, 1f));
        originalMaterial = new Material();
        cube = modelBuilder.createBox(4f, 4f, 4f,
                new Material(ColorAttribute.createDiffuse(0.9f, 0.9f, 0.9f, 0f)),
                Usage.Position | Usage.Normal);
        float x = -7.5f;
        while (x <= 7.5f) {
            float y = -7.5f;
            while (y <= 7.5f) {
                float z = -7.5f;
                while (z <= 7.5f) {
                    CubeObject cubeInstance = new CubeObject(cube);
                    cubeInstance.transform.setToTranslation(x,y,z);
                    cubeInstances.add(cubeInstance);
                    if (x == -7.5f || x == 7.5f) z += 5f;
                    else if (y == -7.5 || y == 7.5) z += 5f;
                    else z += 15f;
                } y += 5f;
            } x += 5f;
        }

    } // Create end

    @Override
    public void render() {

        cam3d.update();
        camController.update();
        stage.act(Gdx.graphics.getDeltaTime());

        // Current player
        if (player == 0) {
            playerButton1.addAction(Actions.removeActor());
            stage.addActor(playerButton1);
        }
        else {
            playerButton2.addAction(Actions.removeActor());
            stage.addActor(playerButton2);
        }

        // Game end buttons
        if (selectedList.size() >= 56) {
            stage.addActor(drawButton);
            Gdx.input.setInputProcessor(stage);
        }
        if (gameWon) {
            if      (player == 0) stage.addActor(gameWonButton2);
            else if (player == 1) stage.addActor(gameWonButton1);
            Gdx.input.setInputProcessor(stage);
        }

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        // Cubes and lights
        modelBatch.begin(cam3d);
        for (final CubeObject cubeInstance : cubeInstances) {
            if (isVisible(cam3d, cubeInstance)) {
                modelBatch.render(cubeInstance, environment);
            }
        }
        modelBatch.end();

        // Score Board
        scoreBoardBuilder.setLength(0);
        scoreBoardBuilder.append(score1).append(" - ").append(score2);
        scoreBoard.setText(scoreBoardBuilder);

        // Debugging label
        debugLabelBuilder.setLength(0);
        debugLabelBuilder.append(" FPS: ").append(Gdx.graphics.getFramesPerSecond());
        debugLabelBuilder.append("   Selected: ").append(selected);
        debugLabelBuilder.append("   inner:").append(inner);
        debugLabelBuilder.append("   listToCheck").append(listToCheck);
        debugLabel.setText(debugLabelBuilder);

        stage.draw();

    } // Render end

    private boolean isVisible(final Camera cam3d, final CubeObject cubeInstance) {
        // Viewing-frustum culling
        cubeInstance.transform.getTranslation(position);
        position.add(cubeInstance.center);
        return cam3d.frustum.sphereInFrustum(position, cubeInstance.radius);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        selecting = getObject(screenX, screenY);
        return selecting >= 0;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (selecting >= 0) {
            if (selecting == getObject(screenX, screenY)) {

                // Check if cube is already used
                cubeIsUsed = false;
                for (int j = 0; j < selectedList.size(); j++) {
                    if (selecting == selectedList.get(j)) {
                        cubeIsUsed = true;
                        break;
                    }
                }
                if (!cubeIsUsed) {
                    setSelected(selecting);
                    selectedList.add(selected);
                    if (player == 0) {
                        player1List.add(selected);
                        listToCheck = player1List;
                    }
                    else if (player == 1) {
                        player2List.add(selected);
                        listToCheck = player2List;
                    }

                    // what an ugly mess (but it fucking works)
                    inner.clear();    inner.add(0);inner.add(1);inner.add(2);inner.add(3);
                    if (listToCheck.containsAll(inner))     gameWon = true;
                    if (!gameWon) {
                        inner.clear();inner.add(0);inner.add(4);inner.add(8);inner.add(12);
                        if (listToCheck.containsAll(inner)) gameWon = true;
                    }
                    if (!gameWon) {
                        inner.clear();inner.add(0);inner.add(5);inner.add(10);inner.add(15);
                        if (listToCheck.containsAll(inner)) gameWon = true;
                    }
                    if (!gameWon) {
                        inner.clear();inner.add(0);inner.add(16);inner.add(28);inner.add(40);
                        if (listToCheck.containsAll(inner)) gameWon = true;
                    }
                    if (!gameWon) {
                        inner.clear();inner.add(0);inner.add(20);inner.add(34);inner.add(52);
                        if (listToCheck.containsAll(inner)) gameWon = true;
                    }
                    if (!gameWon) {
                        inner.clear();inner.add(0);inner.add(17);inner.add(30);inner.add(43);
                        if (listToCheck.containsAll(inner)) gameWon = true;
                    }
                    if (!gameWon) {
                        inner.clear();inner.add(1);inner.add(5);inner.add(9);inner.add(13);
                        if (listToCheck.containsAll(inner)) gameWon = true;
                    }
                    if (!gameWon) {
                        inner.clear();inner.add(1);inner.add(17);inner.add(29);inner.add(4);
                        if (listToCheck.containsAll(inner)) gameWon = true;
                    }
                    if (!gameWon) {
                        inner.clear();inner.add(2);inner.add(6);inner.add(10);inner.add(14);
                        if (listToCheck.containsAll(inner)) gameWon = true;
                    }
                    if (!gameWon) {
                        inner.clear();inner.add(2);inner.add(18);inner.add(30);inner.add(42);
                        if (listToCheck.containsAll(inner)) gameWon = true;
                    }
                    if (!gameWon) {
                        inner.clear();inner.add(3);inner.add(6);inner.add(9);inner.add(12);
                        if (listToCheck.containsAll(inner)) gameWon = true;
                    }
                    if (!gameWon) {
                        inner.clear();inner.add(3);inner.add(7);inner.add(11);inner.add(15);
                        if (listToCheck.containsAll(inner)) gameWon = true;
                    }
                    if (!gameWon) {
                        inner.clear();inner.add(3);inner.add(18);inner.add(29);inner.add(40);
                        if (listToCheck.containsAll(inner)) gameWon = true;
                    }
                    if (!gameWon) {
                        inner.clear();inner.add(3);inner.add(19);inner.add(31);inner.add(43);
                        if (listToCheck.containsAll(inner)) gameWon = true;
                    }
                    if (!gameWon) {
                        inner.clear();inner.add(3);inner.add(21);inner.add(35);inner.add(55);
                        if (listToCheck.containsAll(inner)) gameWon = true;
                    }
                    if (!gameWon) {
                        inner.clear();inner.add(4);inner.add(5);inner.add(6);inner.add(7);
                        if (listToCheck.containsAll(inner)) gameWon = true;
                    }
                    if (!gameWon) {
                        inner.clear();inner.add(4);inner.add(20);inner.add(32);inner.add(44);
                        if (listToCheck.containsAll(inner)) gameWon = true;
                    }
                    if (!gameWon) {
                        inner.clear();inner.add(7);inner.add(21);inner.add(33);inner.add(47);
                        if (listToCheck.containsAll(inner)) gameWon = true;
                    }
                    if (!gameWon) {
                        inner.clear();inner.add(8);inner.add(9);inner.add(10);inner.add(11);
                        if (listToCheck.containsAll(inner)) gameWon = true;
                    }
                    if (!gameWon) {
                        inner.clear();inner.add(8);inner.add(22);inner.add(34);inner.add(48);
                        if (listToCheck.containsAll(inner)) gameWon = true;
                    }
                    if (!gameWon) {
                        inner.clear();inner.add(11);inner.add(23);inner.add(35);inner.add(51);
                        if (listToCheck.containsAll(inner)) gameWon = true;
                    }
                    if (!gameWon) {
                        inner.clear();inner.add(12);inner.add(13);inner.add(14);inner.add(15);
                        if (listToCheck.containsAll(inner)) gameWon = true;
                    }
                    if (!gameWon) {
                        inner.clear();inner.add(12);inner.add(22);inner.add(32);inner.add(40);
                        if (listToCheck.containsAll(inner)) gameWon = true;
                    }
                    if (!gameWon) {
                        inner.clear();inner.add(12);inner.add(24);inner.add(36);inner.add(52);
                        if (listToCheck.containsAll(inner)) gameWon = true;
                    }
                    if (!gameWon) {
                        inner.clear();inner.add(12);inner.add(25);inner.add(38);inner.add(55);
                        if (listToCheck.containsAll(inner)) gameWon = true;
                    }
                    if (!gameWon) {
                        inner.clear();inner.add(13);inner.add(25);inner.add(37);inner.add(53);
                        if (listToCheck.containsAll(inner)) gameWon = true;
                    }
                    if (!gameWon) {
                        inner.clear();inner.add(14);inner.add(26);inner.add(38);inner.add(54);
                        if (listToCheck.containsAll(inner)) gameWon = true;
                    }
                    if (!gameWon) {
                        inner.clear();inner.add(15);inner.add(23);inner.add(33);inner.add(43);
                        if (listToCheck.containsAll(inner)) gameWon = true;
                    }
                    if (!gameWon) {
                        inner.clear();inner.add(15);inner.add(26);inner.add(37);inner.add(52);
                        if (listToCheck.containsAll(inner)) gameWon = true;
                    }
                    if (!gameWon) {
                        inner.clear();inner.add(15);inner.add(27);inner.add(39);inner.add(55);
                        if (listToCheck.containsAll(inner)) gameWon = true;
                    }
                    if (!gameWon) {
                        inner.clear();inner.add(16);inner.add(17);inner.add(18);inner.add(19);
                        if (listToCheck.containsAll(inner)) gameWon = true;
                    }
                    if (!gameWon) {
                        inner.clear();inner.add(16);inner.add(20);inner.add(22);inner.add(24);
                        if (listToCheck.containsAll(inner)) gameWon = true;
                    }
                    if (!gameWon) {
                        inner.clear();inner.add(19);inner.add(21);inner.add(23);inner.add(27);
                        if (listToCheck.containsAll(inner)) gameWon = true;
                    }
                    if (!gameWon) {
                        inner.clear();inner.add(24);inner.add(25);inner.add(26);inner.add(27);
                        if (listToCheck.containsAll(inner)) gameWon = true;
                    }
                    if (!gameWon) {
                        inner.clear();inner.add(28);inner.add(29);inner.add(30);inner.add(31);
                        if (listToCheck.containsAll(inner)) gameWon = true;
                    }
                    if (!gameWon) {
                        inner.clear();inner.add(28);inner.add(32);inner.add(34);inner.add(36);
                        if (listToCheck.containsAll(inner)) gameWon = true;
                    }
                    if (!gameWon) {
                        inner.clear();inner.add(31);inner.add(33);inner.add(35);inner.add(39);
                        if (listToCheck.containsAll(inner)) gameWon = true;
                    }
                    if (!gameWon) {
                        inner.clear();inner.add(36);inner.add(37);inner.add(38);inner.add(39);
                        if (listToCheck.containsAll(inner)) gameWon = true;
                    }
                    if (!gameWon) {
                        inner.clear();inner.add(40);inner.add(41);inner.add(42);inner.add(43);
                        if (listToCheck.containsAll(inner)) gameWon = true;
                    }
                    if (!gameWon) {
                        inner.clear();inner.add(40);inner.add(44);inner.add(48);inner.add(52);
                        if (listToCheck.containsAll(inner)) gameWon = true;
                    }
                    if (!gameWon) {
                        inner.clear();inner.add(40);inner.add(45);inner.add(50);inner.add(55);
                        if (listToCheck.containsAll(inner)) gameWon = true;
                    }
                    if (!gameWon) {
                        inner.clear();inner.add(41);inner.add(45);inner.add(49);inner.add(53);
                        if (listToCheck.containsAll(inner)) gameWon = true;
                    }
                    if (!gameWon) {
                        inner.clear();inner.add(42);inner.add(46);inner.add(50);inner.add(54);
                        if (listToCheck.containsAll(inner)) gameWon = true;
                    }
                    if (!gameWon) {
                        inner.clear();inner.add(43);inner.add(46);inner.add(49);inner.add(52);
                        if (listToCheck.containsAll(inner)) gameWon = true;
                    }
                    if (!gameWon) {
                        inner.clear();inner.add(43);inner.add(47);inner.add(51);inner.add(55);
                        if (listToCheck.containsAll(inner)) gameWon = true;
                    }
                    if (!gameWon) {
                        inner.clear();inner.add(44);inner.add(45);inner.add(46);inner.add(47);
                        if (listToCheck.containsAll(inner)) gameWon = true;
                    }
                    if (!gameWon) {
                        inner.clear();inner.add(48);inner.add(49);inner.add(50);inner.add(51);
                        if (listToCheck.containsAll(inner)) gameWon = true;
                    }
                    if (!gameWon) {
                        inner.clear();inner.add(52);inner.add(53);inner.add(54);inner.add(55);
                        if (listToCheck.containsAll(inner)) gameWon = true;
                    }

                    if (gameWon) {
                        if (player == 0) {
                            score1 += 1;
                        }
                        else if (player == 1) {
                            score2 += 1;
                        }
                    }

                    // swap player even if game is won, the loser starts the new game
                    player = 1 - player;

                }
            }
            selecting = -1;
            return true;
        }
        return false;
    } // touchUp end

    private void setSelected(int value) {
        selected = value;
        if (selected >= 0) {
            Material mat = cubeInstances.get(selected).materials.get(0);
            originalMaterial.clear();
            originalMaterial.set(mat);
            mat.clear();
            if (player == 0) {
                mat.set(player1Material);
            }
            else if (player == 1) {
                mat.set(player2Material);
            }
        }
    }

    private int getObject(int screenX, int screenY) {
        Ray ray = cam3d.getPickRay(screenX, screenY);
        int   result   = -1;
        float distance = -1;
        for (int i = 0; i < cubeInstances.size; ++i) {
            final CubeObject cubeInstance = cubeInstances.get(i);
            cubeInstance.transform.getTranslation(position);
            position.add(cubeInstance.center);
            float dist2 = ray.origin.dst2(position);
            if (distance >= 0 && dist2 > distance) continue;
            if (Intersector.intersectRaySphere(ray, position, cubeInstance.radius, null)) {
                result   = i;
                distance = dist2;
            }
        }
        return result;
    }

    private void resetGame() {
        // Reset the color of the selected cubes
        for (int i = 0; i < player1List.size(); i++) {
            Material mat = cubeInstances.get(player1List.get(i)).materials.get(0);
            player1Material.clear();
            player1Material.set(mat);
            mat.clear();
            mat.set(originalMaterial);
        }
        for (int i = 0; i < player2List.size(); i++) {
            Material mat = cubeInstances.get(player2List.get(i)).materials.get(0);
            player2Material.clear();
            player2Material.set(mat);
            mat.clear();
            mat.set(originalMaterial);
        }

        // Clear the lists
        player1List.clear();
        player2List.clear();
        selectedList.clear();

        gameWon = false;

        Gdx.input.setInputProcessor(new InputMultiplexer(this, camController));

        // Remove the game end buttons
        gameWonButton2.addAction(Actions.removeActor());
        gameWonButton1.addAction(Actions.removeActor());
        drawButton.addAction(Actions.removeActor());
    }

    @Override
    public void dispose() {
        cube.dispose();
        stage.dispose();
        modelBatch.dispose();
        cubeInstances.clear();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void pause() {
    }

    public void resume() {
    }

}
