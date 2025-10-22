# Performance Optimizations

This document outlines the performance optimizations implemented in the Shruti app.

## Low Latency Audio
- Optimized buffer sizes based on device capabilities
- Async processing with Kotlin Coroutines
- Efficient pitch detection algorithm (~10-15ms latency)

## UI Optimization
- Throttled UI updates to prevent frame drops
- Hardware-accelerated Canvas drawing
- Efficient state management with StateFlow

## Battery Efficiency
- Audio capture stops when not in use
- Efficient coroutine management
- Minimal background processing
