package org.firstinspires.ftc.teamcode.subsystems;

import static org.firstinspires.ftc.teamcode.Constants.Arm.kClawClosed;
import static org.firstinspires.ftc.teamcode.Constants.Arm.kClawName;
import static org.firstinspires.ftc.teamcode.Constants.Arm.kClawOpen;
import static org.firstinspires.ftc.teamcode.Constants.Arm.kLeftSlideName;
import static org.firstinspires.ftc.teamcode.Constants.Arm.kRightSlideName;
import static org.firstinspires.ftc.teamcode.Constants.Arm.kSlideBottom;
import static org.firstinspires.ftc.teamcode.Constants.Arm.kSlideD;
import static org.firstinspires.ftc.teamcode.Constants.Arm.kSlideG;
import static org.firstinspires.ftc.teamcode.Constants.Arm.kSlideHigh;
import static org.firstinspires.ftc.teamcode.Constants.Arm.kSlideHub;
import static org.firstinspires.ftc.teamcode.Constants.Arm.kSlideLow;
import static org.firstinspires.ftc.teamcode.Constants.Arm.kSlideMiddle;
import static org.firstinspires.ftc.teamcode.Constants.Arm.kSlideP;
import static org.firstinspires.ftc.teamcode.Constants.Arm.kSlideS;
import static org.firstinspires.ftc.teamcode.Constants.Arm.kSlideStackDistance;
import static org.firstinspires.ftc.teamcode.Constants.Arm.kSlideTolerance;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.shplib.commands.Subsystem;
import org.firstinspires.ftc.teamcode.shplib.controllers.ElevatorFFController;
import org.firstinspires.ftc.teamcode.shplib.hardware.SHPMotor;
import org.firstinspires.ftc.teamcode.shplib.hardware.units.MotorUnit;

public class ArmSubsystem extends Subsystem {
    private final Servo claw;
    //    private final DistanceSensor poleSensor;
    private final SHPMotor leftSlide;
    private final SHPMotor rightSlide;

    private int stackIndex = 4;

    public enum State {
        BOTTOM, HUB, LOW, MIDDLE, HIGH, STACK
    }

    private State state;

    public ArmSubsystem(HardwareMap hardwareMap) {
        claw = hardwareMap.get(Servo.class, kClawName);

//        poleSensor = hardwareMap.get(DistanceSensor.class, "coneSensor");

        leftSlide = new SHPMotor(hardwareMap, kLeftSlideName);
        leftSlide.reverseDirection();
        leftSlide.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        leftSlide.enablePositionPID(kSlideP, kSlideD);
        leftSlide.setPositionErrorTolerance(kSlideTolerance);
        leftSlide.enableFF(new ElevatorFFController(kSlideS, kSlideG));

        rightSlide = new SHPMotor(hardwareMap, kRightSlideName);
//        rightSlide.reverseDirection();
        rightSlide.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        rightSlide.enablePositionPID(kSlideP, kSlideD);
        rightSlide.setPositionErrorTolerance(kSlideTolerance);
        rightSlide.enableFF(new ElevatorFFController(kSlideS, kSlideG));

        setState(State.BOTTOM);
    }

    public void setState(State state) {
        this.state = state;
    }

    public double getDriveBias() {
        return Math.abs(getSlidePosition(MotorUnit.TICKS) / kSlideHigh - 1.0);
    }

    public void openClaw() {
        if (clawClosed()) claw.setPosition(kClawOpen);
        if (atStacks()) stackIndex++;
    }

    public void closeClaw() {
        if (!clawClosed()) claw.setPosition(kClawClosed);
        if (atStacks() && stackIndex > 0) stackIndex--;
    }

    public boolean clawClosed() {
        return claw.getPosition() == kClawClosed;
    }

//    public boolean isOverPole() {
//        return poleSensor.getDistance(DistanceUnit.INCH) <= 6.0;
//    }

    public boolean atStacks() {
        return state == State.STACK;
    }

    public boolean atSetpoint() {
        return leftSlide.atPositionSetpoint() && rightSlide.atPositionSetpoint();
    }

    public double getSlidePosition(MotorUnit unit) {
        return (leftSlide.getPosition(unit) + rightSlide.getPosition(unit)) / 2.0;
    }

    private double processState() {
        switch (state) {
            case BOTTOM:
//                telemetry.addData("Power: ", slide.setPosition(10.0));
                leftSlide.setPosition(kSlideBottom);
                return rightSlide.setPosition(kSlideBottom);
//                break;
            case HUB:
                leftSlide.setPosition(kSlideHub);
                return rightSlide.setPosition(kSlideHub);
            case LOW:
                leftSlide.setPosition(kSlideLow);
                return rightSlide.setPosition(kSlideLow);
//                break;
            case MIDDLE:
                leftSlide.setPosition(kSlideMiddle);
                return rightSlide.setPosition(kSlideMiddle);
//                break;
            case HIGH:
                leftSlide.setPosition(kSlideHigh);
                return rightSlide.setPosition(kSlideHigh);
//                break;
            case STACK:
                leftSlide.setPosition(stackIndex * kSlideStackDistance + kSlideBottom);
                return rightSlide.setPosition(stackIndex * kSlideStackDistance + kSlideBottom);
        }
        return 0.0;
    }

    @Override
    public void periodic(Telemetry telemetry) {
//        telemetry.addData("Slide State: ", state.toString());
        telemetry.addData("Num Cones Stacked: ", stackIndex + 1);
        telemetry.addData("Left Slide Position: ", leftSlide.getPosition(MotorUnit.TICKS));
        telemetry.addData("Right Slide Position: ", rightSlide.getPosition(MotorUnit.TICKS));
//        telemetry.addData("Pole Distance (in): ", poleSensor.getDistance(DistanceUnit.INCH));
        telemetry.addData("Right Slide PID Output: ", processState());
    }
}
