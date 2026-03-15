import paho.mqtt.client as mqtt
from paho.mqtt.client import CallbackAPIVersion
import time
import random
import json
import threading

# Configuration
BROKER = "172.16.40.10" # Default IoT Gateway IP
PORT = 1883
TOPIC_BASE = "quickstay/iot/"

# List of simulated devices (Room access, sensors)
DEVICES = [
    {"id": "device_001", "type": "access_control", "propiedad_id": 101},
    {"id": "device_002", "type": "temperature", "propiedad_id": 101},
    {"id": "device_003", "type": "access_control", "propiedad_id": 102},
    {"id": "device_004", "type": "temperature", "propiedad_id": 102},
]

def on_connect(client, userdata, flags, rc):
    if rc == 0:
        print(f"Connected to MQTT Broker!")
    else:
        print(f"Failed to connect, return code {rc}")

def simulate_device(device):
    client = mqtt.Client(CallbackAPIVersion.VERSION1, device["id"])
    client.on_connect = on_connect
    
    try:
        # Connect to broker (Use localhost if standard IP fails for testing)
        try:
            client.connect(BROKER, PORT, 60)
        except:
            print(f"Could not connect to {BROKER}, trying localhost...")
            client.connect("localhost", PORT, 60)

        client.loop_start()

        while True:
            # Generate random telemetry
            payload = None

            if device["type"] == "temperature":
                payload = {
                    "device_id": device["id"],
                    "propiedad_id": device["propiedad_id"],
                    "value": round(random.uniform(18.0, 26.0), 2),
                    "unit": "C",
                    "timestamp": time.time()
                }
            elif device["type"] == "access_control":
                # Simulate random entry events
                if random.random() > 0.8: # Occasional event
                    payload = {
                        "device_id": device["id"],
                        "propiedad_id": device["propiedad_id"],
                        "event": "door_opened",
                        "authorized": True,
                        "timestamp": time.time()
                    }
            
            if payload:
                topic = f"{TOPIC_BASE}{device['type']}/{device['id']}"
                client.publish(topic, json.dumps(payload))
                print(f"Published to {topic}: {payload}")

            time.sleep(random.uniform(2, 10))

    except Exception as e:
        print(f"Error in device {device['id']}: {e}")

if __name__ == "__main__":
    print("Starting QuickStay IoT Simulator...")
    threads = []
    for dev in DEVICES:
        t = threading.Thread(target=simulate_device, args=(dev,))
        t.start()
        threads.append(t)
    
    # Keep main thread alive
    for t in threads:
        t.join()
