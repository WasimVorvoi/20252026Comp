using UnityEngine;

public class LapManager : MonoBehaviour
{
    public RacePlayer racePlayer;
    public int totalLaps = 3;
    public int currentLap = 1;
    public int currentCheckpoint;

    public void Update()
    {
        if (racePlayer)
        {
            currentLap = racePlayer.currentLap;
            currentCheckpoint = racePlayer.nextCheckpoint;

            NetworkRaceManager manager = NetworkRaceManager.instance;
            if (manager)
            {
                totalLaps = manager.totalLaps;
            }
        }
    }
}
