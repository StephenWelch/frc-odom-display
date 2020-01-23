import com.team254.lib.geometry.Pose2d;
import com.team254.lib.geometry.Translation2d;

import javafx.scene.canvas.GraphicsContext;

public abstract class ADrawable {

    /**
     * Draws a Pose scaled by the given aspect ratio onto the given GraphicsContext
     * @param gc The GraphicsContext to draw on
     * @param pose The pose to draw
     * @param aspectRatio The aspect ratio to scale the pose by before drawing
     */
    public abstract void draw(GraphicsContext gc, Pose2d pose, Translation2d aspectRatio);

    /**
     * Draws a Pose onto a given GraphicsContext
     * @param gc The GraphicsContext to draw on
     * @param pose The pose to draw
     */
    public void draw(GraphicsContext gc, Pose2d pose){}

}
