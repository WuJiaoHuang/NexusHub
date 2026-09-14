import Taro from '@tarojs/taro'

export interface CampusLocation {
  latitude: number
  longitude: number
  source: 'device' | 'default'
}

export const DEFAULT_CAMPUS_LOCATION: CampusLocation = {
  longitude: 120.1542,
  latitude: 30.2871,
  source: 'default'
}

export async function getCurrentLocation(): Promise<CampusLocation> {
  try {
    const location = await Taro.getLocation({ type: 'gcj02' })
    return {
      latitude: location.latitude,
      longitude: location.longitude,
      source: 'device'
    }
  } catch {
    return DEFAULT_CAMPUS_LOCATION
  }
}
