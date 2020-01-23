# FRC Odometry Display

This is a simple application capable of displaying a robot's position on the field via odometry readings. 
It can also display the target trajectory, if there is one.

[Screenshot]: https://github.com/StephenWelch/raw/master/frc-odom-display/screenshot.png

## How to Run
Run `./gradlew run`

## Setup
- Units are **meters** for coordinates and **degrees** for heading
- Put the keys `Time`, `Odometry X`, `Odometry Y`, and `Odometry Heading` into the `SmartDashboard` table to see the robot's position on the field
  - You can either use `SmartDashboard.putNumber()` or `NetworkTableInstance.getDefault().getTable("SmartDashboard").getEntry("<Key here>").setDouble(<Value here>)`.
    You choose. 
- Put the keys `Target X`, `Target Y`, and `Target Heading` into the `SmartDashboard` table to see the target trajectory displayed.

