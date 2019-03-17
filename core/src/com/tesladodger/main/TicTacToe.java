package com.tesladodger.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
//import com.badlogic.gdx.scenes.scene2d.Event;
//import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
//import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
//import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Camera;
//import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
//import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;

import java.util.Vector;


public class TicTacToe extends InputAdapter implements ApplicationListener {

    //Logic
    private int visibleCount;
    private int player = 0; // 'actual' player - 1

    //Camera
    private PerspectiveCamera cam3d;
    private CameraInputController camController;

    //Cubes
    public static class CubeObject extends ModelInstance {
        public final Vector3 center = new Vector3();
        public final Vector3 dimensions = new Vector3();
        public final float radius;
        private final static BoundingBox bounds = new BoundingBox();
        public CubeObject(Model model) {
            super(model);
            calculateBoundingBox(bounds);
            bounds.getCenter(center);
            bounds.getDimensions(dimensions);
            radius = dimensions.len() / 2f;
        }
    }
    private Vector<Integer> selectedVec = new Vector();
    private Vector3 position = new Vector3();
    private int selected = -1, selecting = -1;
    private boolean cubeIsUsed;
    private Material player1Material;
    private Material player2Material;
    private Material originalMaterial;
    private ModelBatch modelBatch;
    private Model cube;
    private Array<CubeObject> cubeInstances = new Array<CubeObject>();
    private Environment environment;

    //Stage Stuff
    private Stage stage;
    //private Texture resetBtnTexture;
    //private TextureRegion resetBtnTextureRegion;
    //private TextureRegionDrawable resetBtnTextureRegionDrawable;
    //private ImageButton resetBtn;
    private Label label;
    private BitmapFont font;
    private StringBuilder strBuilder;

    @Override
    public void create() {
        //Stage
        //resetBtnTexture = new Texture(Gdx.files.internal("reset.png"));
        //resetBtnTextureRegion = new TextureRegion(resetBtnTexture);
        //resetBtnTextureRegionDrawable = new TextureRegionDrawable(resetBtnTextureRegion);
        //resetBtn = new ImageButton(resetBtnTextureRegionDrawable);
        //resetBtn.addListener(new EventListener() {
            //@Override
            //public boolean handle(Event event) {
                //return false;
            //}
        //});

        font = new BitmapFont();
        label = new Label(" ", new Label.LabelStyle(font, Color.WHITE));
        strBuilder = new StringBuilder();

        stage = new Stage();
        stage.addActor(label);
        //stage.addActor(resetBtn);
        Gdx.input.setInputProcessor(stage);
        
        //3D Stuff
        modelBatch = new ModelBatch();

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

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, 1f, 0.8f, 0.2f));

        ModelBuilder modelBuilder = new ModelBuilder();
        player1Material = new Material();
        player1Material.set(ColorAttribute.createDiffuse(0f, 0f, .7f, 1f));
        player2Material = new Material();
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
                    if (x == -7.5f || x == 7.5f) {
                        z += 5f;
                    }
                    else if (y == -7.5 || y == 7.5) {
                        z += 5f;
                    }
                    else {
                        z += 15f;
                    }
                }
                y += 5f;
            }
            x += 5f;
        }

    } //Create end

    @Override
    public void render() {
        //Logic
        cam3d.update();
        camController.update();
        stage.act(Gdx.graphics.getDeltaTime());

        //Drawing
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        visibleCount = 0;
        modelBatch.begin(cam3d);
        for (final CubeObject cubeInstance : cubeInstances) {
            if (isVisible(cam3d, cubeInstance)) {
                modelBatch.render(cubeInstance, environment);
                visibleCount++;
            }
        }
        modelBatch.end();

        strBuilder.setLength(0);
        strBuilder.append(" FPS: ").append(Gdx.graphics.getFramesPerSecond());
        strBuilder.append("   Visible: ").append(visibleCount);
        strBuilder.append("   Selected: ").append(selected);
        strBuilder.append("   Player: ").append(player+1);
        label.setText(strBuilder);
        stage.draw();

    } //Render end

    private boolean isVisible(final Camera cam3d, final CubeObject cubeInstance) {
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
                cubeIsUsed = false;
                for (int j = 0; j < selectedVec.size(); j++) {
                    if (selecting == selectedVec.get(j)) {
                        cubeIsUsed = true;
                    }
                }
                if (!cubeIsUsed) {
                    setSelected(selecting, player);
                    selectedVec.add(selected);

                    //todo check if player wins here

                    player = 1 - player;
                }
            }
            selecting = -1;
            return true;
        }
        return false;
    }

    private void setSelected(int value, int player) {
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
        int result = -1;
        float distance = -1;
        for (int i = 0; i < cubeInstances.size; ++i) {
            final CubeObject cubeInstance = cubeInstances.get(i);
            cubeInstance.transform.getTranslation(position);
            position.add(cubeInstance.center);
            float dist2 = ray.origin.dst2(position);
            if (distance >= 0 && dist2 > distance) continue;
            if (Intersector.intersectRaySphere(ray, position, cubeInstance.radius, null)) {
                result = i;
                distance = dist2;
            }
        }
        return result;
    }

    @Override
    public void dispose() {
        cube.dispose();
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
