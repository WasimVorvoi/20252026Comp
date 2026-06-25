using UnityEngine;

public class RaceTimerHighScore : MonoBehaviour
{
    private const string BestTimeKey = "VRRace_BestTime";

    private float elapsed;
    private bool timing;
    private bool savedThisRace;

    public float Elapsed
    {
        get { return elapsed; }
        private set { elapsed = value; }
    }

    public float BestTime
    {
        get { return PlayerPrefs.GetFloat(BestTimeKey, 0f); }
        set
        {
            float existing = PlayerPrefs.GetFloat(BestTimeKey, 0f);
            if (existing <= 0f || value < existing)
            {
                PlayerPrefs.SetFloat(BestTimeKey, value);
                PlayerPrefs.Save();
            }
        }
    }

    public void Update()
    {
        NetworkRaceManager manager = NetworkRaceManager.instance;
        if (manager == false)
        {
            return;
        }

        if (manager.raceStarted && manager.raceFinished == false)
        {
            timing = true;
        }

        if (timing && manager.raceFinished == false)
        {
            Elapsed = Elapsed + Time.deltaTime;
        }

        if (manager.raceFinished && savedThisRace == false)
        {
            timing = false;
            savedThisRace = true;
            BestTime = Elapsed;
        }

        if (manager.countdownRunning)
        {
            Elapsed = 0f;
            savedThisRace = false;
        }
    }

    public string FormatTime(float seconds)
    {
        int minutes = (int)(seconds / 60f);
        float remainder = seconds - minutes * 60f;
        return minutes.ToString("00") + ":" + remainder.ToString("00.000");
    }
}
