import com.team254.lib.geometry.Pose2d;
import com.team254.lib.geometry.Rotation2d;
import com.team254.lib.geometry.Translation2d;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import us.ilite.robot.auto.paths.RobotDimensions;

public class DrawableRobot extends ADrawable {

    // The points defining the outline of the robot.
    private Translation2d[] outlinePoints;
    // One path for the left wheels, one for the right wheels
    private DrawablePath leftPath = new DrawablePath(Color.RED, 50);
    private DrawablePath rightPath = new DrawablePath(Color.RED, 50);

    /**
     * Constructs a DrawableRobot with a given outline
     * @param outlinePoints The points defining the outline of the robot
     */
    public DrawableRobot(Translation2d...outlinePoints) {
        this.outlinePoints = outlinePoints;
    }

    public void draw(GraphicsContext gc, Pose2d pose, Translation2d aspectRatio, boolean drawLeftRight) {
        // Rotates outline points by robot rotation, scales them by aspect ratio, and stores in array
        Translation2d[] pointsToDraw = new Translation2d[outlinePoints.length];
        for(int pointIndex = 0; pointIndex < outlinePoints.length; pointIndex++) {
            pointsToDraw[pointIndex] = outlinePoints[pointIndex].rotateBy(pose.getRotation());
            pointsToDraw[pointIndex] = pointsToDraw[pointIndex].translateBy(pose.getTranslation());
            pointsToDraw[pointIndex] = new Translation2d(pointsToDraw[pointIndex].x() * aspectRatio.x(), pointsToDraw[pointIndex].y() * aspectRatio.y());
        }

        // Draw left/right paths first then draw robot on top

        // Determine where to start drawing left/right paths from
        Translation2d leftSide = outlinePoints[0].translateBy(new Translation2d(RobotDimensions.kSideToCenter, 0)).rotateBy(pose.getRotation()).translateBy(pose.getTranslation());
        Translation2d rightSide = outlinePoints[1].translateBy(new Translation2d(RobotDimensions.kSideToCenter, 0)).rotateBy(pose.getRotation()).translateBy(pose.getTranslation());
        leftSide = new Translation2d(leftSide.x() * aspectRatio.x(), leftSide.y() * aspectRatio.y());
        rightSide = new Translation2d(rightSide.x() * aspectRatio.x(), rightSide.y() * aspectRatio.y());

        // Draw paths from left/right side points
        if(drawLeftRight) {
            leftPath.draw(gc, new Pose2d(leftSide, Rotation2d.fromDegrees(0.0)));
            rightPath.draw(gc, new Pose2d(rightSide, Rotation2d.fromDegrees(0.0)));
        }

        // Draw the rotated robot outline we stored earlier
        gc.setStroke(Color.BLACK);
        gc.beginPath();
        gc.moveTo(pointsToDraw[0].x(), pointsToDraw[0].y());
        for(int pointIndex = 1; pointIndex <= pointsToDraw.length; pointIndex++) {
            gc.lineTo(pointsToDraw[pointIndex % pointsToDraw.length].x(), pointsToDraw[pointIndex % pointsToDraw.length].y());
        }
        gc.stroke();
        gc.closePath();
    }

    @Override
    public void draw(GraphicsContext gc, Pose2d pose, Translation2d aspectRatio) {
        draw(gc, pose, aspectRatio, false);
    }


    /**
     * Clears robot from screen
     */
    public void clear() {
        leftPath.clear();
        rightPath.clear();
    }

}
