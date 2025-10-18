#!/usr/bin/env python3
"""
Simple test script to verify the ecommerce microservices are working
"""
import requests
import json
import time
import threading
import subprocess
import os
import signal

class EcommerceTest:
    def __init__(self):
        self.security_url = "http://localhost:8081"
        self.gateway_url = "http://localhost:8080"
        self.processes = []
        
    def start_service(self, service_name, port, working_dir):
        """Start a Spring Boot service"""
        print(f"Starting {service_name} on port {port}...")
        cmd = ["java", "-jar", f"target/{service_name}-1.0.0.jar", f"--server.port={port}"]
        
        # First build the service
        build_cmd = ["mvn", "clean", "package", "-DskipTests"]
        subprocess.run(build_cmd, cwd=working_dir, check=True)
        
        # Then start it
        process = subprocess.Popen(cmd, cwd=working_dir)
        self.processes.append(process)
        return process
        
    def wait_for_service(self, url, timeout=60):
        """Wait for a service to be ready"""
        start_time = time.time()
        while time.time() - start_time < timeout:
            try:
                response = requests.get(f"{url}/actuator/health", timeout=5)
                if response.status_code == 200:
                    return True
            except:
                pass
            time.sleep(2)
        return False
        
    def test_authentication(self):
        """Test the authentication flow"""
        print("Testing authentication...")
        
        # Register a new user
        register_data = {
            "email": "test@example.com",
            "password": "password123",
            "firstName": "Test",
            "lastName": "User"
        }
        
        try:
            response = requests.post(f"{self.security_url}/api/auth/register", 
                                   json=register_data, timeout=10)
            print(f"Register response: {response.status_code}")
            
            # Login
            login_data = {
                "email": "test@example.com",
                "password": "password123"
            }
            
            response = requests.post(f"{self.security_url}/api/auth/login", 
                                   json=login_data, timeout=10)
            print(f"Login response: {response.status_code}")
            
            if response.status_code == 200:
                token = response.json().get("token")
                print(f"Received JWT token: {token[:50]}..." if token else "No token received")
                return token
                
        except Exception as e:
            print(f"Authentication test failed: {e}")
            
        return None
        
    def test_gateway_routing(self, token):
        """Test gateway routing with authentication"""
        print("Testing gateway routing...")
        
        if not token:
            print("No token available for gateway test")
            return
            
        headers = {"Authorization": f"Bearer {token}"}
        
        try:
            # Test routing to security service through gateway
            response = requests.get(f"{self.gateway_url}/security/api/auth/profile", 
                                  headers=headers, timeout=10)
            print(f"Gateway auth test response: {response.status_code}")
            
        except Exception as e:
            print(f"Gateway test failed: {e}")
            
    def cleanup(self):
        """Stop all services"""
        print("Stopping services...")
        for process in self.processes:
            process.terminate()
            try:
                process.wait(timeout=10)
            except subprocess.TimeoutExpired:
                process.kill()
                
    def run_tests(self):
        """Run all tests"""
        try:
            # Start security service
            security_dir = os.path.join(os.getcwd(), "backend", "security-service")
            gateway_dir = os.path.join(os.getcwd(), "backend", "gateway")
            
            self.start_service("security-service", 8081, security_dir)
            
            # Wait for security service to be ready
            if not self.wait_for_service(self.security_url):
                print("Security service failed to start")
                return
                
            print("Security service is ready!")
            
            # Test authentication
            token = self.test_authentication()
            
            # Start gateway service
            self.start_service("gateway", 8080, gateway_dir)
            
            # Wait for gateway to be ready
            if not self.wait_for_service(self.gateway_url):
                print("Gateway service failed to start")
                return
                
            print("Gateway service is ready!")
            
            # Test gateway routing
            self.test_gateway_routing(token)
            
            print("All tests completed!")
            
        except KeyboardInterrupt:
            print("Tests interrupted by user")
        except Exception as e:
            print(f"Test failed: {e}")
        finally:
            self.cleanup()

if __name__ == "__main__":
    test = EcommerceTest()
    test.run_tests()