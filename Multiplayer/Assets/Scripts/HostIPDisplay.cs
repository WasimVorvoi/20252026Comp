using UnityEngine;
using Mirror;
using System.Net;
using System.Net.Sockets;

public class HostIPDisplay : MonoBehaviour
{
    string localIP;
    bool isHosting;

    GUIStyle labelStyle;

    void Update()
    {
        bool serverActive = NetworkServer.active;
        if (serverActive && !isHosting)
        {
            localIP = GetLocalIP();
            isHosting = true;
        }
        else if (!serverActive && isHosting)
        {
            isHosting = false;
        }
    }

    void OnGUI()
    {
        if (!isHosting) return;

        if (labelStyle == null)
        {
            labelStyle = new GUIStyle(GUI.skin.box);
            labelStyle.fontSize = 20;
            labelStyle.normal.textColor = Color.white;
            labelStyle.alignment = TextAnchor.MiddleCenter;
            labelStyle.padding = new RectOffset(10, 10, 6, 6);
        }

        string text = $"Your IP: {localIP}";
        Vector2 size = labelStyle.CalcSize(new GUIContent(text));
        float x = (Screen.width - size.x) / 2f;
        GUI.Box(new Rect(x, 10, size.x, size.y), text, labelStyle);
    }

    static string GetLocalIP()
    {
        try
        {
            using Socket socket = new Socket(AddressFamily.InterNetwork, SocketType.Dgram, ProtocolType.Udp);
            socket.Connect("8.8.8.8", 80);
            return ((IPEndPoint)socket.LocalEndPoint).Address.ToString();
        }
        catch
        {
            return "Unknown";
        }
    }
}
