import com.team254.lib.geometry.Pose2d;
import com.team254.lib.geometry.Rotation2d;
import com.team254.lib.geometry.Translation2d;
import edu.wpi.first.networktables.EntryListenerFlags;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.File;

public class FieldWindow extends Application {

    private static final NetworkTable
        kSmartDashboard = NetworkTableInstance.getDefault().getTable("SmartDashboard");

    private Image fieldImage;
    private Canvas fieldCanvas;
    private GraphicsContext fieldContext;
    private Text clock;
    private Text connectionStatus;
    private Text mouseXInches, mouseYInches;

    private Translation2d fieldInchesToPixels;
    private DrawableRobot drawableRobot = new DrawableRobot(new Translation2d(-RobotDimensions.kBackToCenter, -RobotDimensions.kSideToCenter),
                                                         new Translation2d(-RobotDimensions.kBackToCenter, RobotDimensions.kSideToCenter),
                                                         new Translation2d(RobotDimensions.kFrontToCenter, RobotDimensions.kSideToCenter),
                                                         new Translation2d(RobotDimensions.kFrontToCenter, -RobotDimensions.kSideToCenter));
    private DrawablePath robotPath = new DrawablePath(Color.BLUE);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        // Create layout containers for UI elements (buttons, text, etc.)
        BorderPane root = new BorderPane();
        VBox sidePane = new VBox(5);
        HBox bottomPane = new HBox();
        Scene scene = new Scene(root, 800, 600);

        // Configure layout
        sidePane.setAlignment(Pos.TOP_CENTER);
        sidePane.setPadding(new Insets(10, 10, 10, 10));

        // Create UI elements
        clock = new Text("0.0");
        connectionStatus = new Text("Disconnected");
        mouseXInches = new Text("X");
        mouseYInches = new Text("Y");
        try {
            fieldImage = new Image(new File("field.png").toURI().toURL().toExternalForm(), 640, 480, true, false);
        } catch (Exception pE) {
            pE.printStackTrace();
        }
        fieldCanvas = new Canvas(fieldImage.getWidth(), fieldImage.getHeight());
        // Sets up a conversion factor so that we can convert from pixels->inches and vice-versa
        fieldInchesToPixels = new Translation2d( fieldCanvas.getWidth() / (27.0 * 12.0), fieldCanvas.getHeight() / (27.0 * 12.0));
        fieldContext = fieldCanvas.getGraphicsContext2D();

        // Configure UI elements and listeners
        fieldContext.setLineWidth(2.0);
        // Updates text in the corner of the screen with the exact X-Y position of the cursor on the field in inches
        fieldCanvas.setOnMouseMoved(e -> {
            double mouseXInchesVal = e.getX()/fieldInchesToPixels.x();
            double mouseYInchesVal = Math.abs(e.getY()) / fieldInchesToPixels.y();

            mouseXInches.setText("X: " + mouseXInchesVal);
            mouseYInches.setText("Y: " + mouseYInchesVal);
        });

        NetworkTableInstance.getDefault().startClientTeam(1885);
//        NetworkTableInstance.getDefault().startClient("localhost");


        // Set up a listener on the "Time" key in NetworkTables.
        // When the key is updated, pull odometry information from NetworkTables and draw to the screen
        NetworkTableInstance.getDefault().addEntryListener(kSmartDashboard.getEntry("Time"), entryNotification -> {
                Platform.runLater(() -> {
                    OdometryData data = getFromNt();
                    drawData(data);
                });
            },
            EntryListenerFlags.kNew | EntryListenerFlags.kImmediate | EntryListenerFlags.kUpdate | EntryListenerFlags.kLocal);

        // Set up a listener that updates connection status text when the laptop connects or disconnects from NetworkTables
        // The "immediateNotify" flag means that the listener will be called immediately with the current connection state instead
        // of waiting for a connection change.
        NetworkTableInstance.getDefault().addConnectionListener(connectionNotification -> {
            String text;
            if(connectionNotification.connected) {
                text = "Connected - " + connectionNotification.conn.remote_ip;
            } else {
                text = "Disconnected";
            }
            connectionStatus.setText(text);
        }, true);

        resetAll();

        // Add UI elements to appropriate layout containers
        sidePane.getChildren().addAll(clock, connectionStatus);
        bottomPane.getChildren().addAll(mouseXInches, mouseYInches);
        root.setCenter(fieldCanvas);
        root.setRight(sidePane);
        root.setBottom(bottomPane);
        primaryStage.setOnCloseRequest(e -> System.exit(0));
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    /**
     * Clears the field canvas of anything drawn and re-draws the image of the field
     */
    public void resetField() {
        fieldContext.clearRect(0.0, 0.0, fieldCanvas.getWidth(), fieldCanvas.getHeight());
        fieldContext.drawImage(fieldImage, 0.0, 0.0);
    }


    /**
     * Resets everything
     */
    public void resetAll() {
        drawableRobot.clear();
        robotPath.clear();
        drawTime(0.0);
        resetField();
    }


    /**
     * Draws data from a OdometryData object onto the screen.
     * @param pNextDataToDraw The odometry data to draw
     */
    public void drawData(OdometryData pNextDataToDraw) {
        resetField();
        Pose2d robotPose = normalizePoseToField(pNextDataToDraw.current_pose);
        Pose2d targetPose = normalizePoseToField(pNextDataToDraw.target_pose);

        drawableRobot.draw(fieldContext, robotPose, fieldInchesToPixels, true);
        robotPath.draw(fieldContext, targetPose, fieldInchesToPixels);

        drawTime(pNextDataToDraw.time);
    }


    /**
     * Ensures that the coordinate frame and units for the poses transmitted from the robot and the pose as displayed on the screen are the same.
     * This application defines the origin of the field as the upper-left corner of the field with units as inches (like PathWeaver).
     * @param pose The pose to translate to the field's coordinate system.
     * @return The normalized Pose.
     */
    private Pose2d normalizePoseToField(Pose2d pose) {
        // Convert odometry data from meters (reported by robot) to inches (what we draw on the field)
        Translation2d normalizedTranslation = new Translation2d(39.37 * pose.getTranslation().x(), 39.37 * pose.getTranslation().y());
        Rotation2d normalizedRotation = pose.getRotation();

        return new Pose2d(normalizedTranslation, normalizedRotation);
    }


    /**
     * Updates the time text with the current time
     * @param time The time to draw
     */
    private void drawTime(double time) {
        clock.setText("Sim Time: " + String.format("%.2f", time));
    }


    /**
     * Retrieves odometry data from NetworkTables
     * @return An OdometryData object
     */
    private OdometryData getFromNt() {
        double odomX = kSmartDashboard.getEntry("Odometry X").getDouble(0.0);
        double odomY = kSmartDashboard.getEntry("Odometry Y").getDouble(0.0);
        double odomHeading = kSmartDashboard.getEntry("Odometry Heading").getDouble(0.0);
        double time = kSmartDashboard.getEntry("Time").getDouble(0.0);

        double targetX = kSmartDashboard.getEntry("Target X").getDouble(0.0);
        double targetY = kSmartDashboard.getEntry("Target Y").getDouble(0.0);
        double targetHeading = kSmartDashboard.getEntry("Target Heading").getDouble(0.0);

        return new OdometryData(
                new Pose2d(odomX, odomY, Rotation2d.fromDegrees(odomHeading)),
                new Pose2d(targetX, targetY, Rotation2d.fromDegrees(targetHeading)),
                time
        );
    }

}
