using Mirror;
using UnityEngine;
using UnityEngine.UI;

public class LobbyUI : MonoBehaviour
{
    public Button hostButton;
    public Button joinButton;
    public Button readyButton;
    public Button startRaceButton;
    public Button quitButton;
    public InputField ipInput;
    public Text playerCountText;
    public Text readyText;
    public Text readyButtonText;

    public RacePlayer localPlayer;

    public void Update()
    {
        FindLocalPlayer();
        UpdateLobbyText();
    }

    public void HostGame()
    {
        if (NetworkManager.singleton)
        {
            NetworkManager.singleton.StartHost();
        }
    }

    public void JoinGame()
    {
        if (NetworkManager.singleton)
        {
            NetworkManager.singleton.networkAddress = ipInput.text;
            NetworkManager.singleton.StartClient();
        }
    }

    public void ToggleReady()
    {
        if (localPlayer)
        {
            localPlayer.CmdSetReady(localPlayer.isReady == false);
        }
    }

    public void StartRace()
    {
        SimpleNetworkManager manager = NetworkManager.singleton as SimpleNetworkManager;
        if (NetworkServer.active && manager)
        {
            manager.StartRaceFromMenu();
        }
    }

    public void QuitGame()
    {
        if (NetworkServer.active && NetworkClient.isConnected)
        {
            NetworkManager.singleton.StopHost();
            return;
        }

        if (NetworkClient.isConnected)
        {
            NetworkManager.singleton.StopClient();
            return;
        }

        Application.Quit();
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

    public void UpdateLobbyText()
    {
        int count = 0;
        RacePlayer[] players = GameObject.FindObjectsOfType<RacePlayer>();
        int readyCount = 0;
        int i = 0;
        while (i < players.Length)
        {
            count = count + 1;
            if (players[i].isReady)
            {
                readyCount = readyCount + 1;
            }

            i = i + 1;
        }

        playerCountText.text = "PLAYERS  " + count.ToString() + " / 8";
        readyText.text = "READY  " + readyCount.ToString() + " / " + count.ToString();

        if (localPlayer && readyButtonText)
        {
            if (localPlayer.isReady)
            {
                readyButtonText.text = "READY";
            }
            else
            {
                readyButtonText.text = "NOT READY";
            }
        }
    }
}
