using Mirror;
using UnityEngine;
using UnityEngine.InputSystem;

public class VRKartController : NetworkBehaviour
{
    public Rigidbody body;

    public InputActionReference throttleAction;
    public InputActionReference brakeAction;
    public InputActionReference steerAction;

    public float speed;
    public float maxSpeed = 18f;
    public float maxReverseSpeed = 8f;
    public float acceleration = 12f;
    public float brakeForce = 24f;
    public float steeringPower = 280f;
    public float smoothTurn = 20f;

    public float throttleInput;
    public float brakeInput;
    public float headYawInput;

    public void Start()
    {
        if (body)
        {
            body.isKinematic = isClientOnly;
        }

        EnableAction(throttleAction);
        EnableAction(brakeAction);
        EnableAction(steerAction);
    }

    public void EnableAction(InputActionReference reference)
    {
        if (reference && reference.action != null)
        {
            reference.action.Enable();
        }
    }

    public float ReadAction(InputActionReference reference)
    {
        if (reference && reference.action != null)
        {
            return reference.action.ReadValue<float>();
        }

        return 0f;
    }

    public void Update()
    {
        if (isLocalPlayer)
        {
            float throttle = ReadAction(throttleAction);
            float brake = ReadAction(brakeAction);
            float steer = ReadAction(steerAction);
            float targetYaw = transform.eulerAngles.y + steer * 45f;
            CmdSetDrivingInput(throttle, brake, targetYaw);
        }
    }

    [Command]
    public void CmdSetDrivingInput(float throttle, float brake, float headYaw)
    {
        throttleInput = Mathf.Clamp01(throttle);
        brakeInput = Mathf.Clamp01(brake);
        headYawInput = headYaw;
    }

    public void FixedUpdate()
    {
        if (isServer)
        {
            ServerMoveKart();
        }
    }

    [Server]
    public void ServerResetKart()
    {
        speed = 0f;
        throttleInput = 0f;
        brakeInput = 0f;

        if (body)
        {
            body.linearVelocity = Vector3.zero;
            body.angularVelocity = Vector3.zero;
        }
    }

    [Server]
    public void ServerMoveKart()
    {
        NetworkRaceManager manager = NetworkRaceManager.instance;
        if (manager == false)
        {
            return;
        }

        if (manager.raceStarted == false || manager.raceFinished)
        {
            if (manager.countdownRunning)
            {
                ServerResetKart();
            }

            speed = Mathf.MoveTowards(speed, 0f, brakeForce * Time.fixedDeltaTime);
            ApplyMovement();
            return;
        }

        speed = speed + throttleInput * acceleration * Time.fixedDeltaTime;
        speed = speed - brakeInput * brakeForce * Time.fixedDeltaTime;
        speed = Mathf.Clamp(speed, -maxReverseSpeed, maxSpeed);

        float steerAmount = Mathf.DeltaAngle(transform.eulerAngles.y, headYawInput);
        steerAmount = Mathf.Clamp(steerAmount, -45f, 45f);
        float turn = steerAmount / 45f;

        Quaternion wantedRotation = Quaternion.Euler(0f, transform.eulerAngles.y + turn * steeringPower * Time.fixedDeltaTime, 0f);
        transform.rotation = Quaternion.Slerp(transform.rotation, wantedRotation, smoothTurn * Time.fixedDeltaTime);

        ApplyMovement();
    }

    public void ApplyMovement()
    {
        Vector3 velocity = transform.forward * speed;
        velocity.y = body.linearVelocity.y;
        body.linearVelocity = velocity;
    }
}
