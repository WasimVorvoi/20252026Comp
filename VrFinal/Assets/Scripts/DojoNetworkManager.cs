using UnityEngine;
using Mirror;

public class DojoNetworkManager : NetworkManager
{
    public float dojoSpacing;

    public override void OnServerAddPlayer(NetworkConnectionToClient conn)
    {
        int index = numPlayers;
        Vector3 spawnPos = new Vector3(index * dojoSpacing, 0f, 0f);
        GameObject dojo = Instantiate(playerPrefab, spawnPos, Quaternion.identity);
        NetworkServer.AddPlayerForConnection(conn, dojo);
    }
}
