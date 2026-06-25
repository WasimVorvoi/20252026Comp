using Mirror;
using UnityEngine;

public class RacePlayer : NetworkBehaviour
{
    [SyncVar] public string playerName = "Player";
    [SyncVar] public int playerNumber;
    [SyncVar] public bool isReady;
    [SyncVar] public int currentLap = 1;
    [SyncVar] public int nextCheckpoint;
    [SyncVar] public bool finished;
    [SyncVar] public int racePosition;
    [SyncVar] public float distanceAlongRace;

    public LapManager lapManager;
    public Camera playerCamera;
    public GameObject leftController;
    public GameObject rightController;

    private bool cameraDone;

    public void Start()
    {
        DontDestroyOnLoad(gameObject);

        if (lapManager)
        {
            lapManager.racePlayer = this;
        }
    }

    public void Update()
    {
        if (cameraDone == false)
        {
            cameraDone = true;
            SetupCamera();
        }

        if (isServer)
        {
            NetworkRaceManager manager = NetworkRaceManager.instance;
            if (manager)
            {
                manager.AddPlayer(this);
            }

            distanceAlongRace = currentLap * 1000f + nextCheckpoint * 50f;
        }
    }

    public void SetupCamera()
    {
        if (playerCamera)
        {
            playerCamera.gameObject.SetActive(isLocalPlayer);
        }

        if (isLocalPlayer)
        {
            DisableOtherCameras();
            CmdSetPlayerName("Player " + netId.ToString());
        }
    }

    public void DisableOtherCameras()
    {
        Camera[] cameras = GameObject.FindObjectsOfType<Camera>();
        int i = 0;
        while (i < cameras.Length)
        {
            if (cameras[i] != playerCamera)
            {
                cameras[i].gameObject.SetActive(false);
            }

            i = i + 1;
        }
    }

    [Command]
    public void CmdSetReady(bool ready)
    {
        isReady = ready;
    }

    [Command]
    public void CmdSetPlayerName(string newName)
    {
        playerName = newName;
    }

    [Server]
    public void ServerResetForRace()
    {
        currentLap = 1;
        nextCheckpoint = 0;
        finished = false;
        racePosition = 0;
        distanceAlongRace = 0f;
    }

    [Server]
    public void ServerCheckpointHit(int checkpointIndex)
    {
        NetworkRaceManager manager = NetworkRaceManager.instance;
        CheckpointManager checkpoints = CheckpointManager.instance;

        if (finished || manager == false || checkpoints == false)
        {
            return;
        }

        if (manager.raceStarted == false || checkpointIndex != nextCheckpoint)
        {
            return;
        }

        nextCheckpoint = nextCheckpoint + 1;

        if (nextCheckpoint >= checkpoints.GetCheckpointCount())
        {
            nextCheckpoint = 0;
            currentLap = currentLap + 1;
            CheckForWin(manager);
        }
    }

    [Server]
    public void CheckForWin(NetworkRaceManager manager)
    {
        if (currentLap > manager.totalLaps)
        {
            currentLap = manager.totalLaps;
            finished = true;
            manager.PlayerFinished(this);
        }
    }

    public void OnDestroy()
    {
        NetworkRaceManager manager = NetworkRaceManager.instance;
        if (manager)
        {
            manager.RemovePlayer(this);
        }
    }
}
