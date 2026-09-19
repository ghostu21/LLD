package com.gdrive.lld.operation;

/**
 * Template method for a filesystem mutation: validate → perform → log.
 * <p>
 * Subclasses fill in the steps; {@link #execute()} is synchronized so two
 * threads cannot interleave the same command object.
 */
public abstract class FileOperation {
    public final void execute() {
        synchronized (this) {
            if (!validate()) {
                throw new IllegalStateException("Validation failed. Operation aborted.");
            }
            performOperation();
            logOperation();
        }
    }

    public void undo() {
        throw new UnsupportedOperationException("Undo not supported for " + getClass().getSimpleName());
    }

    protected abstract boolean validate();

    protected abstract void performOperation();

    protected void logOperation() {
        System.out.println("Operation performed: " + getClass().getSimpleName());
    }
}
