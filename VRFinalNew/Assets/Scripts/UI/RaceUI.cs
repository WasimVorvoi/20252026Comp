using UnityEngine;
using UnityEngine.UI;

public class RaceUI : MonoBehaviour
{
    public Text lapText;
    public Text positionText;
    public Text timeText;
    public Text bestText;
    public Text countdownText;
    public Text rankingsText;
    public Text finishedText;
    public GameObject finishPanel;

    public RacePlayer localPlayer;
    public RaceTimerHighScore timer;

    public void Update()
    {
        FindLocalPlayer();
        UpdateText();
    }

    public void FindLocalPlayer()
    {
        if (localPlayer)
        {
            return;
        }

        RacePlayer[] players = GameObject.FindObjectsOfType<RacePlayer>();
        int i = 0;
        while (i < players.Length)
        {
            if (players[i].isLocalPlayer)
            {
                localPlayer = players[i];
                return;
            }

            i = i + 1;
        }
    }

    public void UpdateText()
    {
        NetworkRaceManager manager = NetworkRaceManager.instance;

        if (localPlayer && manager)
        {
            lapText.text = "LAP  " + localPlayer.currentLap.ToString() + " / " + manager.totalLaps.ToString();
            positionText.text = "POS  " + localPlayer.racePosition.ToString();
        }

        if (timer)
        {
            timeText.text = "TIME  " + timer.FormatTime(timer.Elapsed);
            float best = timer.BestTime;
            if (best > 0f)
            {
                bestText.text = "BEST  " + timer.FormatTime(best);
            }
            else
            {
                bestText.text = "BEST  --:--";
            }
        }

        if (manager == false)
        {
            return;
        }

        if (manager.countdownRunning)
        {
            countdownText.text = Mathf.CeilToInt(manager.countdown).ToString();
            countdownText.color = new Color(1f, 0.82f, 0.1f);
        }
        else if (manager.raceStarted && manager.raceFinished == false)
        {
            countdownText.text = "GO!";
            countdownText.color = new Color(0.4f, 1f, 0.4f);
        }
        else
        {
            countdownText.text = "";
        }

        rankingsText.text = manager.rankingText;

        if (finishPanel)
        {
            finishPanel.SetActive(manager.raceFinished);
        }

        if (manager.raceFinished)
        {
            finishedText.text = "FINISH!\n" + manager.winnerName + " WINS";
        }
    }
}
