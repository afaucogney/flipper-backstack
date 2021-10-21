# Actions

## Update React Dependencies

```shell script
npm update --force
npm audit fix --force
```


## Pack for manual install (in official Flipper app)

```shell script
yarn pack
```

## Dynamic load of plugin

do not forget to add path of under developpement plugin in config.json file

https://fbflipper.com/docs/extending/loading-custom-plugins


## Start flipper from source

```shell script
git fetch
git pull
cd desktop
yarn start
```

In case of issue : brew reinstall icu4c
yarn install


## Update version on JitPack

- Update version number on build.gradle ()
- Git commit, push

## Update version on Npm

- Update version number on package.json
- Delete binary and lock file in desktop plugin
- npm install --force
- (npm pack)
- Git commit, push
- npm login
- npm publish