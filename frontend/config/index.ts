import { defineConfig } from '@tarojs/cli'
import path from 'node:path'

export default defineConfig(async () => ({
  projectName: 'CampusLink',
  date: '2026-09-14',
  designWidth: 375,
  deviceRatio: {
    640: 2.34 / 2,
    750: 1,
    828: 1.81 / 2,
    375: 2
  },
  sourceRoot: 'src',
  outputRoot: `dist/${process.env.TARO_ENV}`,
  plugins: ['@tarojs/plugin-framework-react'],
  framework: 'react',
  compiler: 'webpack5',
  alias: {
    '@': path.resolve(__dirname, '..', 'src')
  },
  mini: {
    postcss: {
      pxtransform: {
        enable: true,
        config: {}
      },
      cssModules: {
        enable: false
      }
    }
  },
  h5: {
    publicPath: '/',
    staticDirectory: 'static',
    router: {
      mode: 'browser'
    },
    devServer: {
      port: 10086
    }
  }
}))
