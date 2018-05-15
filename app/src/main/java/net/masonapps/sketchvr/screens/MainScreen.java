package net.masonapps.sketchvr.screens;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.google.vr.sdk.controller.Controller;

import net.masonapps.sketchvr.SolidModelingGame;
import net.masonapps.sketchvr.Style;
import net.masonapps.sketchvr.actions.AddAction;
import net.masonapps.sketchvr.actions.ColorAction;
import net.masonapps.sketchvr.actions.RemoveAction;
import net.masonapps.sketchvr.actions.TransformAction;
import net.masonapps.sketchvr.actions.UndoRedoCache;
import net.masonapps.sketchvr.controller.ViewControlsVirtualStage;
import net.masonapps.sketchvr.environment.Grid;
import net.masonapps.sketchvr.modeling.SketchMeshBuilder;
import net.masonapps.sketchvr.modeling.SketchNode;
import net.masonapps.sketchvr.modeling.SketchProjectEntity;
import net.masonapps.sketchvr.modeling.transform.RotateWidget;
import net.masonapps.sketchvr.modeling.transform.ScaleWidget;
import net.masonapps.sketchvr.modeling.transform.SimpleGrabControls;
import net.masonapps.sketchvr.modeling.transform.TransformWidget3D;
import net.masonapps.sketchvr.modeling.transform.TranslateWidget;
import net.masonapps.sketchvr.modeling.ui.AddNodeInput;
import net.masonapps.sketchvr.modeling.ui.EditModeTable;
import net.masonapps.sketchvr.modeling.ui.FreeDrawInput;
import net.masonapps.sketchvr.modeling.ui.InputProcessorChooser;
import net.masonapps.sketchvr.modeling.ui.MainInterface;
import net.masonapps.sketchvr.modeling.ui.ModelingInputProcessor;
import net.masonapps.sketchvr.modeling.ui.MultiNodeSelector;
import net.masonapps.sketchvr.modeling.ui.PlanarPointsInput;
import net.masonapps.sketchvr.modeling.ui.SingleNodeSelector;
import net.masonapps.sketchvr.ui.ExportDialog;
import net.masonapps.sketchvr.ui.GroupCompleteDialog;

import org.masonapps.libgdxgooglevr.GdxVr;
import org.masonapps.libgdxgooglevr.gfx.AABBTree;
import org.masonapps.libgdxgooglevr.gfx.Entity;
import org.masonapps.libgdxgooglevr.gfx.VrWorldScreen;
import org.masonapps.libgdxgooglevr.gfx.World;
import org.masonapps.libgdxgooglevr.input.DaydreamButtonEvent;
import org.masonapps.libgdxgooglevr.input.DaydreamTouchEvent;
import org.masonapps.libgdxgooglevr.input.VrInputProcessor;
import org.masonapps.libgdxgooglevr.ui.VrInputMultiplexer;
import org.masonapps.libgdxgooglevr.utils.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bob Mason on 12/20/2017.
 */

public class MainScreen extends VrWorldScreen implements SolidModelingGame.OnControllerBackPressedListener {

    public static final float MIN_Z = 0.5f;
    public static final float MAX_Z = 10f;
    private static final String TAG = MainScreen.class.getSimpleName();
    private static final float UI_ALPHA = 0.25f;
    private final MainInterface mainInterface;
    private final UndoRedoCache undoRedoCache;
    private final ShapeRenderer shapeRenderer;
    private final Entity gridEntity;
    private final TranslateWidget translateWidget;
    private final RotateWidget rotateWidget;
    private final ScaleWidget scaleWidget;
    private final GroupCompleteDialog groupDialog;
    private final Entity gradientBackground;
    private final ExportDialog exportDialog;
    private final PlanarPointsInput pointInput;
    private final FreeDrawInput freeDrawInput;
    private TransformWidget3D transformUI;
    private float projectScale = 1f;
    private String projectName;
    private final SketchMeshBuilder sketchMeshBuilder;
    @Nullable
    private SketchNode focusedNode = null;
    @Nullable
    private SketchNode selectedNode = null;
    private List<SketchNode> multiSelectNodes = new ArrayList<>();
    private SketchProjectEntity project;
    private Vector3 hitPoint = new Vector3();
    private AABBTree.IntersectionInfo intersectionInfo = new AABBTree.IntersectionInfo();
    private SimpleGrabControls grabControls = new SimpleGrabControls();
    private BoundingBox selectionBox = new BoundingBox();
    private Vector3 tmp = new Vector3();
    private Vector2 vec2 = new Vector2();
    private Grid gridFloor;
    private Vector3 cameraPosition = new Vector3();
    @Nullable
    private SketchNode nodeToAdd = null;
    private InputProcessorChooser inputProcessorChooser;
    private AddNodeInput addNodeInput;
    private SingleNodeSelector singleNodeSelector;
    private MultiNodeSelector multiNodeSelector;
    private VrInputMultiplexer inputMultiplexer;
    private ViewControlsVirtualStage buttonControls;

    public MainScreen(SolidModelingGame game, String projectName) {
        this(game, projectName, new ArrayList<>());
    }

    public MainScreen(SolidModelingGame game, String projectName, List<SketchNode> nodeList) {
        super(game);
        final Skin skin = game.getSkin();
        this.projectName = projectName;
        gradientBackground = Style.newGradientBackground(getVrCamera().far - 1f);
        getWorld().add(gradientBackground);
        gradientBackground.invalidate();
        gridFloor = new Grid(2, skin.getRegion(Style.Drawables.grid), Color.LIGHT_GRAY);

        setBackgroundColor(Color.SKY);
        sketchMeshBuilder = new SketchMeshBuilder();
        project = new SketchProjectEntity(sketchMeshBuilder, nodeList);
        undoRedoCache = new UndoRedoCache();

        final ModelBuilder modelBuilder = new ModelBuilder();

        translateWidget = new TranslateWidget(modelBuilder);
        translateWidget.setVisible(false);

        rotateWidget = new RotateWidget(modelBuilder);
        rotateWidget.setVisible(false);

        scaleWidget = new ScaleWidget(modelBuilder);
        scaleWidget.setVisible(false);

        final TransformWidget3D.OnTransformActionListener transformActionListener = new TransformWidget3D.OnTransformActionListener() {

            TransformAction.Transform oldTransform;

            @Override
            public void onTransformStarted(@NonNull SketchNode entity) {
                oldTransform = entity.getTransform(new TransformAction.Transform());
            }

            @Override
            public void onTransformFinished(@NonNull SketchNode entity) {
                undoRedoCache.save(new TransformAction(entity, oldTransform, entity.getTransform(new TransformAction.Transform())));
                final AABBTree.Node leafNode = entity.getNode();
                if (leafNode != null)
                    leafNode.refit();
                setSelectedNode(entity);
            }
        };
        translateWidget.setListener(transformActionListener);
        rotateWidget.setListener(transformActionListener);
        scaleWidget.setListener(transformActionListener);
        grabControls.setListener(transformActionListener);

        transformUI = translateWidget;

        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true);
        final SpriteBatch spriteBatch = new SpriteBatch();
        manageDisposable(shapeRenderer, spriteBatch);


        addNodeInput = new AddNodeInput(project, node -> Logger.d("node added"));

        multiNodeSelector = new MultiNodeSelector(project, nodes -> {
            selectionBox.inf();
            nodes.forEach(node -> selectionBox.ext(node.getAABB()));
            multiSelectNodes.clear();
            multiSelectNodes.addAll(nodes);
        });

        exportDialog = new ExportDialog(spriteBatch, skin, (projectName1, fileType, transform) -> getSolidModelingGame().exportFile(project, projectName1, fileType, transform));
        exportDialog.setPosition(0, 0, -2);

        final MainInterface.UiEventListener uiEventListener = new MainInterface.UiEventListener() {

            @Override
            public void onAddClicked(String key) {
                // TODO: 4/25/2018 start new mesh 
            }

            @Override
            public void onDeleteClicked() {
                if (selectedNode != null) {
                    project.remove(selectedNode);
                    undoRedoCache.save(new RemoveAction(selectedNode, project));
                    setSelectedNode(null);
                }
            }

            @Override
            public void onDuplicateClicked() {
                if (selectedNode != null) {
                    final SketchNode previewNode = selectedNode.copy();
                    addNodeInput.setPreviewNode(previewNode);
                    addNodeInput.setVisible(true);
                    inputProcessorChooser.setActiveProcessor(addNodeInput);
                }
            }

            @Override
            public void onColorChanged(Color color) {
                if (selectedNode != null) {
                    final ColorAction colorAction = new ColorAction(selectedNode, selectedNode.getDiffuseColor().cpy(), color.cpy(), c -> mainInterface.getColorPicker().setColor(c));
                    undoRedoCache.save(colorAction);
                    selectedNode.setAmbientColor(color);
                    selectedNode.setDiffuseColor(color);
                }
            }

            @Override
            public void onEditModeChanged(EditModeTable.EditMode mode) {
                setEditMode(mode);
            }

            @Override
            public void onUndoClicked() {
                undoRedoCache.undo();
            }

            @Override
            public void onRedoClicked() {
                undoRedoCache.redo();
            }

            @Override
            public void onExportClicked() {
                exportDialog.show();
            }

            @Override
            public void onGroupClicked() {
                groupDialog.show();
                inputProcessorChooser.setActiveProcessor(multiNodeSelector);
            }

            @Override
            public void onUnGroupClicked() {
                if (selectedNode != null && selectedNode.isGroup()) {
                    project.remove(selectedNode);
                    final int n = selectedNode.getChildCount();
                    for (int i = 0; i < n; i++) {
                        final Node child = selectedNode.getChild(i);
                        if (child instanceof SketchNode) {
                            final SketchNode sketchNode = (SketchNode) child;
                            sketchNode.translation.mul(selectedNode.localTransform);
                            sketchNode.rotation.mulLeft(selectedNode.rotation);
                            sketchNode.scale.scl(selectedNode.scale);
                            sketchNode.invalidate();
                            project.add(sketchNode, true);
                        }
                    }
                }
            }
        };
        mainInterface = new MainInterface(spriteBatch, skin, uiEventListener);
        mainInterface.loadWindowPositions(PreferenceManager.getDefaultSharedPreferences(GdxVr.app.getContext()));

        buttonControls = new ViewControlsVirtualStage(project, spriteBatch, skin, 0.75f, transform -> transformUI.setTransform(transform));
        buttonControls.setVisible(false);

        // TODO: 5/9/2018 move to new view controls 
//        final float sliderVal = 1f - (float) Math.sqrt((-projectPosition.z - MIN_Z) / (MAX_Z - MIN_Z));
//        mainInterface.getViewControls().getZoomSlider().setValue(sliderVal);
//        mainInterface.getViewControls().setListener(new ViewControls.ViewControlListener() {
//            @Override
//            public void onViewSelected(Side side) {
//                RotationUtil.rotateToViewSide(snappedRotation, side);
//                final Quaternion rotDiff = Pools.obtain(Quaternion.class);
//                rotDiff.set(rotation).conjugate().mulLeft(snappedRotation);
//                final float angleRad = rotDiff.getAngleRad();
//                final float duration = Math.abs(angleRad < MathUtils.PI ? angleRad : MathUtils.PI2 - angleRad) / MathUtils.PI;
//                Pools.free(rotDiff);
//                rotationAnimator.setDuration(duration);
//                rotationAnimator.start();
//            }
//
//            @Override
//            public void onZoomChanged(float value) {
//                final float z = -MathUtils.lerp(MIN_Z, MAX_Z, (1f - value) * (1f - value));
//                projectPosition.z = z;
//                if (selectedNode != null) {
//                    center.set(selectedNode.getPosition());
//                } else {
//                    center.set(0, 0, 0);
//                }
//                snappedPosition.set(center).scl(-1).mul(rotation).add(projectPosition);
//                position.set(snappedPosition);
//                project.setPosition(position);
//            }
//        });

        groupDialog = new GroupCompleteDialog(spriteBatch, skin, new GroupCompleteDialog.GroupDialogListener() {
            @Override
            public void onCancelClicked() {
                multiSelectNodes.clear();
            }

            @Override
            public void onDoneClicked() {
                final SketchNode group = new SketchNode();
                for (SketchNode node : multiSelectNodes) {
                    project.remove(node);
                    group.addChild(node);
                }
                project.add(group, true);
                multiSelectNodes.clear();
            }
        });
        groupDialog.setPosition(0, -1f, -1.5f);
        groupDialog.setVisible(false);
        mainInterface.addProcessor(groupDialog);

        exportDialog.setVisible(false);
        mainInterface.addProcessor(exportDialog);

        inputProcessorChooser = new InputProcessorChooser();
        mainInterface.addProcessor(inputProcessorChooser);

        gridEntity = new Entity(new ModelInstance(createGrid(modelBuilder, skin, 3f)));
        gridEntity.setLightingEnabled(false);
        getWorld().add(gridEntity).setTransform(project.getTransform());
        gridEntity.setVisible(false);

        getWorld().add(project);
        project.setScale(projectScale);

        // TODO: 3/23/2018 remove test 
        freeDrawInput = new FreeDrawInput(project);
        pointInput = new PlanarPointsInput(project, point -> Logger.d("point added " + point));
        pointInput.getPlane().set(Vector3.Zero, Vector3.Z);
        singleNodeSelector = new SingleNodeSelector(project, this::setSelectedNode);
        inputProcessorChooser.setActiveProcessor(freeDrawInput);

        inputMultiplexer = new VrInputMultiplexer(mainInterface, inputProcessorChooser);
    }

    private static Model createGrid(ModelBuilder builder, Skin skin, float radius) {
//        final Material material = new Material(TextureAttribute.createDiffuse(skin.getRegion(Style.Drawables.grid)), ColorAttribute.createDiffuse(Color.WHITE), FloatAttribute.createAlphaTest(0.25f), IntAttribute.createCullFace(0), new BlendingAttribute(true, 0.5f));
        final Material material = new Material(ColorAttribute.createDiffuse(Color.GOLDENROD), IntAttribute.createCullFace(0), new BlendingAttribute(true, 0.25f));
        return builder.createRect(
                -radius, -radius, 0f,
                radius, -radius, 0f,
                radius, radius, 0f,
                -radius, radius, 0f,
                0f, 1f, 0f,
                material,
                VertexAttributes.Usage.Position | VertexAttributes.Usage.TextureCoordinates
        );
    }

    private static void drawBounds(ShapeRenderer shapeRenderer, BoundingBox bounds) {
        shapeRenderer.box(bounds.min.x, bounds.min.y, bounds.max.z,
                bounds.getWidth(), bounds.getHeight(), bounds.getDepth());
    }

    private void addNode(SketchNode node) {
        project.add(node, true);
        setSelectedNode(node);
        undoRedoCache.save(new AddAction(node, project));
    }

    protected void setEditMode(EditModeTable.EditMode mode) {
        switch (mode) {
            case TRANSLATE:
                transformUI = translateWidget;
                rotateWidget.setVisible(false);
                scaleWidget.setVisible(false);
                transformUI.setVisible(selectedNode != null);
                break;
            case ROTATE:
                transformUI = rotateWidget;
                translateWidget.setVisible(false);
                scaleWidget.setVisible(false);
                transformUI.setVisible(selectedNode != null);
                break;
            case SCALE:
                transformUI = scaleWidget;
                translateWidget.setVisible(false);
                rotateWidget.setVisible(false);
                transformUI.setVisible(selectedNode != null);
                break;
            default:
                transformUI.setVisible(false);
                break;
        }
        transformUI.setEntity(selectedNode, project);
        mainInterface.setEditMode(mode);
        if (transformUI.isVisible())
            inputProcessorChooser.setActiveProcessor(transformUI);
    }

    private SolidModelingGame getSolidModelingGame() {
        return (SolidModelingGame) game;
    }

    @Override
    protected World createWorld() {
        return new World() {

            @Override
            public void update() {
                super.update();
                project.update();
                transformUI.update();
            }

            @Override
            public void render(ModelBatch batch, Environment environment) {
                super.render(batch, environment);
                gridFloor.render(batch);
                transformUI.render(batch);
                addNodeInput.render(batch);
            }
        };
    }

    @Override
    protected void addLights(Array<BaseLight> lights) {
        final DirectionalLight light = new DirectionalLight();
        light.setColor(Color.WHITE);
        light.setDirection(new Vector3(1, -1, -1).nor());
        lights.add(light);
    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {
        final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(GdxVr.app.getContext()).edit();
        mainInterface.saveWindowPositions(editor);
        editor.apply();
    }

    @Override
    public void show() {
        super.show();
        GdxVr.input.setInputProcessor(inputMultiplexer);
        GdxVr.input.addDaydreamControllerListener(inputProcessorChooser);
        getVrCamera().position.set(0, 0f, 0);
        getVrCamera().lookAt(0, 0, -1);
        buttonControls.attachListener();
    }

    @Override
    public void hide() {
        super.hide();
        GdxVr.input.setInputProcessor(null);
        GdxVr.input.removeDaydreamControllerListener(inputProcessorChooser);
        buttonControls.detachListener();
        final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(GdxVr.app.getContext()).edit();
        mainInterface.saveWindowPositions(editor);
        editor.apply();
    }

    private void updateInterfacePosition() {
        gradientBackground.setPosition(getVrCamera().position);
        mainInterface.setTransformable(true);
        mainInterface.setPosition(getVrCamera().position);
        mainInterface.lookAt(tmp.set(getVrCamera().direction).scl(-1).add(getVrCamera().position), getVrCamera().up);
    }

    @Override
    public void update() {
        super.update();
        grabControls.update(hitPoint, project);
        mainInterface.act();
//        Logger.d(GdxVr.graphics.getFramesPerSecond() + "fps");
        buttonControls.act();
    }

    @Nullable
    public SketchNode getSelectedNode() {
        return selectedNode;
    }

    private void setSelectedNode(@Nullable SketchNode entity) {
        selectedNode = entity;
        mainInterface.setEntity(selectedNode);

        if (selectedNode != null) {
            // TODO: 5/9/2018 possibly center view on selected node or group 
            final Color diffuseColor = selectedNode.getDiffuseColor();
            if (diffuseColor != null)
                mainInterface.getColorPicker().setColor(diffuseColor);
        } else {
            transformUI.setVisible(false);
        }
    }

    @Override
    public void render(Camera camera, int whichEye) {
        GdxVr.gl.glEnable(GL20.GL_DEPTH_TEST);
        shapeRenderer.begin();
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.setTransformMatrix(project.getTransform());
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.line(0, 0, 0, 1, 0, 0);
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.line(0, 0, 0, 0, 0, 1);
        shapeRenderer.setColor(Color.BLUE);
        shapeRenderer.line(0, 0, 0, 0, 1, 0);
        shapeRenderer.end();
        GdxVr.gl.glDisable(GL20.GL_DEPTH_TEST);

        super.render(camera, whichEye);

        shapeRenderer.begin();
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.setTransformMatrix(project.getTransform());
//        AABBTree.debugAABBTree(shapeRenderer, project.getAABBTree(), Color.YELLOW);
        transformUI.drawShapes(shapeRenderer);

        if (inputProcessorChooser.getActiveProcessor() instanceof ModelingInputProcessor)
            ((ModelingInputProcessor) inputProcessorChooser.getActiveProcessor()).draw(shapeRenderer);

        shapeRenderer.end();

        mainInterface.draw(camera);
        // TODO: 5/13/2018 uncomment 
//        buttonControls.draw(camera);
    }

    @Override
    public void onControllerBackButtonClicked() {
        if (!mainInterface.onControllerBackButtonClicked()) {
            final VrInputProcessor activeProcessor = inputProcessorChooser.getActiveProcessor();
            if (activeProcessor instanceof AddNodeInput) {
                ((AddNodeInput) activeProcessor).setPreviewNode(null);
            }
            //todo end part on backpress
//            if (activeProcessor instanceof OnBackClickedListener) {
//                ((OnBackClickedListener) activeProcessor).onBackClicked();
//            }
            if (!(activeProcessor instanceof SingleNodeSelector)) {
                inputProcessorChooser.setActiveProcessor(singleNodeSelector);
            } else {
                toggleViewControls();
            }
        }
    }

    private void toggleViewControls() {
        if (buttonControls.isVisible()) {
            buttonControls.setVisible(false);
            mainInterface.setVisible(true);
            getSolidModelingGame().setCursorVisible(true);
        } else {
            buttonControls.setVisible(true);
            mainInterface.setVisible(false);
            getSolidModelingGame().setCursorVisible(false);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        undoRedoCache.clear();
    }

    public String getProjectName() {
        return projectName;
    }

    @Override
    public void onDaydreamControllerUpdate(Controller controller, int connectionState) {
    }

    @Override
    public void onControllerButtonEvent(Controller controller, DaydreamButtonEvent event) {
    }

    @Override
    public void onControllerTouchPadEvent(Controller controller, DaydreamTouchEvent event) {
    }

    public SketchProjectEntity getProject() {
        return project;
    }
}
