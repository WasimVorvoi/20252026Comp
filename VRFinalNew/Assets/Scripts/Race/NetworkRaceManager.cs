using Mirror;
using UnityEngine;

public class NetworkRaceManager : NetworkBehaviour
{
    public static NetworkRaceManager instance;

    public int totalLaps = 3;
    public float countdownStart = 5f;
    public RacePlayer[] players = new RacePlayer[8];
    public int playerCount;

    [SyncVar] public bool countdownRunning;
    [SyncVar] public bool raceStarted;
    [SyncVar] public bool raceFinished;
    [SyncVar] public float countdown;
    [SyncVar] public string winnerName = "";
    [SyncVar] public string rankingText = "";

    public float rankingTimer;

    public void Start()
    {
        instance = this;
    }

    public void Update()
    {
        if (instance == false)
        {
            instance = this;
        }

        if (isServer)
        {
            ServerUpdateRace();
        }
    }

    public void ServerUpdateRace()
    {
        if (countdownRunning)
        {
            countdown = countdown - Time.deltaTime;
            if (countdown <= 0f)
            {
                countdown = 0f;
                countdownRunning = false;
                raceStarted = true;
            }
        }

        rankingTimer = rankingTimer + Time.deltaTime;
        if (rankingTimer > 0.25f)
        {
            rankingTimer = 0f;
            UpdateRankings();
        }
    }

    [Server]
    public void ResetRace()
    {
        countdownRunning = false;
        raceStarted = false;
        raceFinished = false;
        countdown = countdownStart;
        winnerName = "";
        rankingText = "";
        playerCount = 0;

        int i = 0;
        while (i < players.Length)
        {
            players[i] = null;
            i = i + 1;
        }
    }

    [Server]
    public void ServerStartCountdown()
    {
        countdown = countdownStart;
        countdownRunning = true;
        raceStarted = false;
        raceFinished = false;
        winnerName = "";
    }

    [Server]
    public void AddPlayer(RacePlayer player)
    {
        if (player == false)
        {
            return;
        }

        int i = 0;
        while (i < players.Length)
        {
            if (players[i] == player)
            {
                return;
            }

            i = i + 1;
        }

        i = 0;
        while (i < players.Length)
        {
            if (players[i] == false)
            {
                players[i] = player;
                playerCount = CountPlayers();
                return;
            }

            i = i + 1;
        }
    }

    [Server]
    public void RemovePlayer(RacePlayer player)
    {
        int i = 0;
        while (i < players.Length)
        {
            if (players[i] == player)
            {
                players[i] = null;
            }

            i = i + 1;
        }

        playerCount = CountPlayers();
    }

    public int CountPlayers()
    {
        int count = 0;
        int i = 0;
        while (i < players.Length)
        {
            if (players[i])
            {
                count = count + 1;
            }

            i = i + 1;
        }

        return count;
    }

    [Server]
    public void PlayerFinished(RacePlayer player)
    {
        if (player == false)
        {
            return;
        }

        if (raceFinished == false)
        {
            raceFinished = true;
            winnerName = player.playerName;
        }

        UpdateRankings();
    }

    [Server]
    public void UpdateRankings()
    {
        RacePlayer[] sortedPlayers = new RacePlayer[8];
        int i = 0;
        while (i < players.Length)
        {
            sortedPlayers[i] = players[i];
            i = i + 1;
        }

        int a = 0;
        while (a < sortedPlayers.Length)
        {
            int b = a + 1;
            while (b < sortedPlayers.Length)
            {
                RacePlayer first = sortedPlayers[a];
                RacePlayer second = sortedPlayers[b];
                if (ShouldSwap(first, second))
                {
                    sortedPlayers[a] = second;
                    sortedPlayers[b] = first;
                }

                b = b + 1;
            }

            a = a + 1;
        }

        string text = "";
        int position = 1;
        i = 0;
        while (i < sortedPlayers.Length)
        {
            RacePlayer player = sortedPlayers[i];
            if (player)
            {
                player.racePosition = position;
                text = text + position.ToString() + ". " + player.playerName + "  Lap " + player.currentLap.ToString() + "/" + totalLaps.ToString() + "\n";
                position = position + 1;
            }

            i = i + 1;
        }

        rankingText = text;
    }

    public bool ShouldSwap(RacePlayer first, RacePlayer second)
    {
        if (first == false && second)
        {
            return true;
        }

        if (first == false || second == false)
        {
            return false;
        }

        if (second.finished && first.finished == false)
        {
            return true;
        }

        if (second.currentLap > first.currentLap)
        {
            return true;
        }

        if (second.currentLap == first.currentLap && second.nextCheckpoint > first.nextCheckpoint)
        {
            return true;
        }

        if (second.currentLap == first.currentLap && second.nextCheckpoint == first.nextCheckpoint && second.distanceAlongRace > first.distanceAlongRace)
        {
            return true;
        }

        return false;
    }
}
