import com.team254.lib.geometry.Pose2d;
import com.team254.lib.geometry.Translation2d;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.LinkedList;
import java.util.List;

/**
 * A drawable line of arbitrary points and length
 */
public class DrawablePath extends ADrawable {

    private final Color kLineColor;
    private final int kMaxLength;
    private List<Translation2d> mPointList = new LinkedList<>();

    public DrawablePath(Color pKLineColor, int pKMaxLength) {
        kLineColor = pKLineColor;
        kMaxLength = pKMaxLength;
    }

    public DrawablePath(Color pKLineColor) {
        this(pKLineColor, 0);
    }

    @Override
    public void draw(GraphicsContext gc, Pose2d pose, Translation2d aspectRatio) {
        // Add the new point to point "history"
        mPointList.add(pose.getTranslation());

        // Remove old points from "history"
        if(kMaxLength != 0) {
            while(mPointList.size() > kMaxLength) {
                mPointList.remove(0);
            }
        }

        // Draw a line between each given point
        gc.moveTo(mPointList.get(0).x(), mPointList.get(0).y());
        gc.setStroke(kLineColor);
        gc.beginPath();
        for(Translation2d point : mPointList) {
            Translation2d pointToDraw = new Translation2d(point.x() * aspectRatio.x(), point.y() * aspectRatio.y());
            gc.lineTo(pointToDraw.x(), pointToDraw.y());
        }
        gc.stroke();
        gc.closePath();
    }

    @Override
    public void draw(GraphicsContext gc, Pose2d pose) {
        draw(gc, pose, new Translation2d(1.0, 1.0));
    }


    /**
     * Removes all points from a line
     */
    public void clear() {
        mPointList.clear();
    }

}
