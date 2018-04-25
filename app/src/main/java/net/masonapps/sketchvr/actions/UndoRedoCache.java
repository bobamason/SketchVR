package net.masonapps.sketchvr.actions;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Stack;

/**
 * Created by Bob on 7/21/2017.
 */

public class UndoRedoCache {
    private static final int MAX_UNDO_STACK_COUNT = 20;
    private Stack<Action> undoStack = new Stack<>();
    private Stack<Action> redoStack = new Stack<>();

    @Nullable
    public void undo() {
        if (undoStack.empty()) return;
        final Action action = undoStack.pop();
        redoStack.push(action);
        action.undoAction();
    }

    public int getUndoCount() {
        return undoStack.size();
    }

    @Nullable
    public void redo() {
        if (redoStack.empty()) return;
        final Action action = redoStack.pop();
        undoStack.push(action);
        action.undoAction();
    }

    public int getRedoCount() {
        return redoStack.size();
    }

    public void save(@NonNull Action action) {
        if (undoStack.size() >= MAX_UNDO_STACK_COUNT)
            undoStack.remove(0);
        redoStack.clear();
        undoStack.push(action);
    }

    public void clear() {
        undoStack.clear();
        redoStack.clear();
    }

}
