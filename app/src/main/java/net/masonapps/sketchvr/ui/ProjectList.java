package net.masonapps.sketchvr.ui;

import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.data.ModelData;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Disposable;

import org.json.JSONException;
import org.masonapps.libgdxgooglevr.GdxVr;
import org.masonapps.libgdxgooglevr.gfx.AABBTree;
import org.masonapps.libgdxgooglevr.gfx.Entity;
import org.masonapps.libgdxgooglevr.input.DaydreamTouchEvent;
import org.masonapps.libgdxgooglevr.input.VrInputProcessor;
import org.masonapps.libgdxgooglevr.utils.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Created by Bob Mason on 2/8/2018.
 */

public abstract class ProjectList<T> implements VrInputProcessor, Disposable {

    public static final float ITEM_WIDTH = 1f;
    public static final float TOUCHPAD_SCALE = 150f;
    public static final float SPACING = 0.1f;
    private static final float VISIBLE_EXTENT = 4f;
    private static final float HALF_VISIBLE_EXTENT = VISIBLE_EXTENT / 2f;
    private static final float LIST_Y = -0.5f;
    private static final float LIST_Z = -2.25f;
    private final List<T> list;
    private final GestureDetector gestureDetector;
    private final AABBTree aabbTree = new AABBTree();
    private final Vector3 hitPoint = new Vector3();
    private final OnProjectSelectedListener listener;
    private final List<ProjectItem> items;
    private ShapeRenderer shapeRenderer;
    private AABBTree.IntersectionInfo intersetionInfo = new AABBTree.IntersectionInfo();
    @Nullable
    private ProjectItem focusedItem = null;
    private boolean scrolling = false;
    private float scrollX = 0f;
    private boolean isCursorOver = false;
    private float maxScroll = 0f;
    private boolean needsLayout = true;
    private float SPEED = 2f;
    private Set<Integer> loadingIndices = new HashSet<>();

    public ProjectList(List<T> list, OnProjectSelectedListener listener) {
        this.list = list;
        final int n = list.size();
        maxScroll = (ITEM_WIDTH + SPACING) * n - VISIBLE_EXTENT;
        this.listener = listener;
        items = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            items.add(i, null);
            final float itemX = getItemX(i);
            if (isWithinValidRange(itemX))
                init(i);
        }
        final GestureDetector.GestureAdapter gestureAdapter = new GestureDetector.GestureAdapter() {

            public float lastX = 0f;

            @Override
            public boolean touchDown(float x, float y, int pointer, int button) {
                lastX = x;
                return super.touchDown(x, y, pointer, button);
            }

            @Override
            public boolean tap(float x, float y, int count, int button) {
                return false;
            }

            @Override
            public boolean pan(float x, float y, float deltaX, float deltaY) {
//                if (animating) return false;
                scrolling = true;
                scrollX += -(x - lastX) / TOUCHPAD_SCALE * SPEED;
                scrollX = MathUtils.clamp(scrollX, 0, maxScroll);
                lastX = x;
//                invalidate();
                return true;
            }

            @Override
            public boolean panStop(float x, float y, int pointer, int button) {
//                if (animating) return false;
                scrolling = false;
                invalidate();
                return true;
            }

            @Override
            public boolean fling(float velocityX, float velocityY, int button) {
//                if (animating) return false;
                final float vX = velocityX / TOUCHPAD_SCALE;
//                if (vX < -0.85f && currentIndex < itemCount - 1) {
//                    
//                } else if (vX > 0.85f && currentIndex > 0) {
//                    
//                }
                return true;
            }
        };
        gestureDetector = new GestureDetector(gestureAdapter);


        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true);
    }

    private void validate() {
        if (needsLayout) {
            for (int i = 0; i < items.size(); i++) {
                final ProjectItem item = items.get(i);
                final float itemX = getItemX(i);
                if (isWithinValidRange(itemX)) {
                    if (item == null) {
                        init(i);
                    }
                } else {
                    if (Objects.nonNull(item))
                        recycle(item);
                }
            }
            needsLayout = false;
        }
    }

    private boolean isWithinVisibleRange(float itemX) {
        return itemX > -VISIBLE_EXTENT && itemX < VISIBLE_EXTENT;
    }

    private boolean isWithinValidRange(float itemX) {
        return itemX > -VISIBLE_EXTENT - HALF_VISIBLE_EXTENT && itemX < VISIBLE_EXTENT + HALF_VISIBLE_EXTENT;
    }

    private void invalidate() {
        needsLayout = true;
    }

    @Override
    public boolean performRayTest(Ray ray) {
        isCursorOver = false;
        final boolean rayTest = aabbTree.rayTest(ray, intersetionInfo);
        if (rayTest) {
            hitPoint.set(intersetionInfo.hitPoint);
            focusedItem = (ProjectItem) intersetionInfo.object;
            isCursorOver = true;
        }
        return rayTest;
    }

    public void update() {
        validate();
        items.stream().filter(Objects::nonNull)
                .forEach(item -> {
                    final float itemX = getItemX(item.index);
                    item.setVisible(isWithinVisibleRange(itemX));
                    item.setPosition(itemX, LIST_Y, LIST_Z);
                });
    }

    private float getItemX(int i) {
        return (i * (ITEM_WIDTH + SPACING)) - scrollX - HALF_VISIBLE_EXTENT;
    }

    public void render(ModelBatch batch, Environment environment) {
        items.stream().filter(Objects::nonNull).forEach(item -> {
            item.validate();
            if (item.modelInstance != null && item.isVisible()) {
                batch.render(item.modelInstance, environment);
            }
        });
    }

    public void debug(Camera camera) {
        shapeRenderer.begin();
        shapeRenderer.setProjectionMatrix(camera.combined);
//        AABBTree.debugAABBTree(shapeRenderer, aabbTree, Color.CYAN);
        shapeRenderer.setColor(Color.BLACK);
        items.stream().filter(Objects::nonNull).forEach(item -> {
            item.validate();
            if (item.isVisible()) {
                final BoundingBox bb = item.getAABB();
                shapeRenderer.box(bb.min.x, bb.min.y, bb.max.z, bb.getWidth(), bb.getHeight(), bb.getDepth());
            }
        });
        shapeRenderer.end();
    }

    private void init(final int index) {
        if (loadingIndices.contains(index)) return;
        loadingIndices.add(index);
        final T t = list.get(index);
        Logger.d("init " + index);
        final BoundingBox bb = new BoundingBox();
        final CompletableFuture<ModelData> loadModelFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return loadProject(t, bb);
            } catch (IOException | JSONException e) {
                throw new RuntimeException("failed to load project #" + index, e);
            }
        });
        loadModelFuture.exceptionally(e -> {
            GdxVr.app.postRunnable(() -> onLoadFailed(t, e));
            return null;
        }).thenAccept(modelData -> {
            if (modelData != null) {
                GdxVr.app.postRunnable(() -> {
                    loadingIndices.remove(index);

                    final float maxx = Math.max(Math.abs(bb.min.x), Math.abs(bb.max.x));
                    final float maxy = Math.max(Math.abs(bb.min.y), Math.abs(bb.max.y));
                    final float maxz = Math.max(Math.abs(bb.min.z), Math.abs(bb.max.z));
                    bb.set(bb.min.set(-maxx, -maxy, -maxz), bb.max.set(maxx, maxy, maxz));

                    ProjectItem projectItem = new ProjectItem(new ModelInstance(new Model(modelData)), bb, index);
                    projectItem.setPosition(getItemX(index), LIST_Y, LIST_Z);
                    final Vector3 dimens = new Vector3();
                    final float scale = ITEM_WIDTH / bb.getDimensions(dimens).len();
//                    Logger.d("bb dimens " + index + " = " + dimens);
//                    Logger.d("scale " + index + " = " + scale);
                    projectItem.setScale(scale);
                    items.set(index, projectItem);
                    aabbTree.insert(projectItem);
                    invalidate();
                });
            }
        });
    }

    protected abstract ModelData loadProject(T t, BoundingBox bounds) throws IOException, JSONException;

    protected abstract void onLoadFailed(T t, Throwable e);

    private void recycle(ProjectItem projectItem) {
        items.set(projectItem.index, null);
        aabbTree.remove(projectItem);
        projectItem.dispose();
    }

    @Override
    public boolean isCursorOver() {
        return isCursorOver;
    }

    @Nullable
    @Override
    public Vector2 getHitPoint2D() {
        return null;
    }

    @Nullable
    @Override
    public Vector3 getHitPoint3D() {
        return hitPoint;
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (isCursorOver) {
            listener.onProjectSelected(focusedItem);
        }
        return isCursorOver;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return true;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

    public void onControllerTouchPadEvent(DaydreamTouchEvent event) {
        switch (event.action) {
            case DaydreamTouchEvent.ACTION_DOWN:
                gestureDetector.touchDown(event.x * TOUCHPAD_SCALE, event.y * TOUCHPAD_SCALE, 0, 0);
                break;
            case DaydreamTouchEvent.ACTION_MOVE:
                gestureDetector.touchDragged(event.x * TOUCHPAD_SCALE, event.y * TOUCHPAD_SCALE, 0);
                break;
            case DaydreamTouchEvent.ACTION_UP:
                gestureDetector.touchUp(event.x * TOUCHPAD_SCALE, event.y * TOUCHPAD_SCALE, 0, 0);
                break;
        }
    }

    @Override
    public void dispose() {
        items.stream().filter(Objects::nonNull).forEach(this::recycle);
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
            shapeRenderer = null;
        }
    }

    public interface OnProjectSelectedListener {
        void onProjectSelected(ProjectItem item);
    }

    public static class ProjectItem extends Entity implements AABBTree.AABBObject {

        public final int index;
        //        @Nullable
//        public CompletableFuture<ModelData> loadModelFuture = null;
        @Nullable
        private AABBTree.Node node = null;
        private BoundingBox aabb = new BoundingBox();

        public ProjectItem(@Nullable ModelInstance modelInstance, BoundingBox boundingBox, int index) {
            super(modelInstance, boundingBox);
            this.index = index;
        }

        @Override
        public Entity setTransform(Matrix4 transform) {
            super.setTransform(transform);
            if (aabb != null)
                aabb.set(getBounds()).mul(this.transform);
            refitNode();
            return this;
        }

        @Override
        public void recalculateTransform() {
            super.recalculateTransform();
            aabb.set(getBounds()).mul(this.transform);
            refitNode();
        }

        private void refitNode() {
            if (node != null) node.refit();
        }

        @Nullable
        @Override
        public AABBTree.Node getNode() {
            return node;
        }

        @Override
        public void setNode(@Nullable AABBTree.Node node) {
            this.node = node;
        }

        @Override
        public BoundingBox getAABB() {
            validate();
            return aabb;
        }

        @Override
        public boolean rayTest(Ray ray, AABBTree.IntersectionInfo intersection) {
            final boolean rayTest = intersectsRayBounds(ray, intersection.hitPoint);
            if (rayTest) {
                intersection.object = this;
                intersection.t = ray.origin.dst2(intersection.hitPoint);
            }
            return rayTest;
        }

        public ProjectItem copy() {
            return new ProjectItem(modelInstance == null ? null : modelInstance.copy(), new BoundingBox(getBounds()), index);
        }
    }
}
