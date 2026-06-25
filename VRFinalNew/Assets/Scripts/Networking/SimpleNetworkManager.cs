using Mirror;
using UnityEngine;

public class SimpleNetworkManager : NetworkManager
{
    public string raceSceneName = "RaceTrack";
    public int playerCount;

    public override void Start()
    {
        base.Start();
    }

    public override void OnServerAddPlayer(NetworkConnectionToClient conn)
    {
        Transform start = GetStartPosition();
        GameObject playerObject;

        if (start)
        {
            playerObject = Instantiate(playerPrefab, start.position, start.rotation);
        }
        else
        {
            playerObject = Instantiate(playerPrefab);
        }

        RacePlayer player = playerObject.GetComponent<RacePlayer>();
        if (player)
        {
            player.playerNumber = numPlayers + 1;
            player.playerName = "Player " + player.playerNumber.ToString();
        }

        NetworkServer.AddPlayerForConnection(conn, playerObject);
        playerCount = numPlayers;
    }

    public override void OnServerSceneChanged(string sceneName)
    {
        base.OnServerSceneChanged(sceneName);

        if (sceneName == raceSceneName)
        {
            NetworkRaceManager manager = NetworkRaceManager.instance;
            if (manager == false)
            {
                manager = GameObject.FindObjectOfType<NetworkRaceManager>();
                NetworkRaceManager.instance = manager;
            }

            if (manager)
            {
                manager.ResetRace();
            }

            SetupPlayersForRace();

            if (manager)
            {
                manager.ServerStartCountdown();
            }
        }
    }

    public override void OnServerReady(NetworkConnectionToClient conn)
    {
        base.OnServerReady(conn);

        if (conn.identity == false)
        {
            return;
        }

        if (IsRaceSceneActive())
        {
            SetupPlayersForRace();
        }
    }

    public void StartRaceFromMenu()
    {
        if (NetworkServer.active)
        {
            ServerChangeScene(raceSceneName);
        }
    }

    public void SetupPlayersForRace()
    {
        RacePlayer[] players = GameObject.FindObjectsOfType<RacePlayer>();
        System.Array.Sort(players, CompareByNetId);

        NetworkRaceManager manager = NetworkRaceManager.instance;

        int index = 0;
        while (index < players.Length)
        {
            RacePlayer player = players[index];
            if (player)
            {
                PlaceAtStart(player, index);

                if (manager)
                {
                    manager.AddPlayer(player);
                }
            }

            index = index + 1;
        }
    }

    public int CompareByNetId(RacePlayer a, RacePlayer b)
    {
        if (a == null || b == null)
        {
            return 0;
        }

        return a.netId.CompareTo(b.netId);
    }

    public void PlaceAtStart(RacePlayer player, int index)
    {
        if (player == false)
        {
            return;
        }

        if (index < 0)
        {
            index = 0;
        }

        Transform startSpot = GetRaceStartPosition(index);
        if (startSpot)
        {
            player.transform.position = startSpot.position;
            player.transform.rotation = startSpot.rotation;
        }
        else
        {
            player.transform.position = new Vector3(5f, 1f, -20f - index * 3f);
            player.transform.rotation = Quaternion.Euler(0f, 90f, 0f);
        }

        player.ServerResetForRace();
    }

    public Transform GetRaceStartPosition(int index)
    {
        if (startPositions != null && startPositions.Count > 0)
        {
            return startPositions[index % startPositions.Count];
        }

        NetworkStartPosition[] found = GameObject.FindObjectsOfType<NetworkStartPosition>();
        if (found.Length > 0)
        {
            return found[index % found.Length].transform;
        }

        return null;
    }

    public bool IsRaceSceneActive()
    {
        return UnityEngine.SceneManagement.SceneManager.GetActiveScene().name == raceSceneName;
    }
}
