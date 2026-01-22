import sys
import time
import socket
import math
import os

# Default emulator port
PORT = 5554
HOST = "localhost"

# Starting location (Istanbul roughly)
START_LAT = 41.0082
START_LON = 28.9784

def get_auth_token():
    try:
        from os.path import expanduser
        home = expanduser("~")
        with open(f"{home}/.emulator_console_auth_token", "r") as f:
            return f.read().strip()
    except:
        return None

def main():
    token = get_auth_token()
    
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    try:
        s.connect((HOST, PORT))
    except ConnectionRefusedError:
        print(f"Could not connect to emulator at {HOST}:{PORT}")
        print("Make sure the emulator is running.")
        return

    # Read initial welcome message
    data = s.recv(1024).decode()
    
    if token:
        # Auth
        s.send(f"auth {token}\n".encode())
        auth_resp = s.recv(1024).decode()
        if "OK" not in auth_resp:
             print(f"Auth failed: {auth_resp}")
             # Some emulators don't require auth or handle it differently, proceed anyway
    
    print("Starting mock ride...")
    print("Press Ctrl+C to stop.")
    
    # Simulate a circle ride
    center_lat = START_LAT
    center_lon = START_LON
    radius = 0.002 # approx 200-300 meters radius
    
    angle = 0
    try:
        while True:
            # Calculate new position
            # Simple approximation
            lat = center_lat + radius * math.sin(math.radians(angle))
            # Adjust longitude for latitude (cos(lat))
            lon = center_lon + (radius * math.cos(math.radians(angle)) / math.cos(math.radians(center_lat)))
            
            cmd = f"geo fix {lon} {lat}\n"
            s.send(cmd.encode())
            
            if angle % 30 == 0:
                print(f"Sent location: {lat:.6f}, {lon:.6f}")
            
            angle = (angle + 10) % 360
            time.sleep(1) # 1 update per second
    except KeyboardInterrupt:
        print("\nStopping mock ride.")
    finally:
        s.close()

if __name__ == "__main__":
    main()
