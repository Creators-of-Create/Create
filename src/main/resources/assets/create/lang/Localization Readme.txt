Hi there! 
Looking to contribute with a localization? Here's how:


1. Check if your language is already present in an outdated state:

https://github.com/Creators-of-Create/Create/blob/mc1.16/dev/src/generated/resources/assets/create/lang/unfinished
Shows generated lang files that are composed of any old translation file in the official lang folder as well as placeholder entries of new content added since.
NOTICE: this is not the true lang folder, changes to the files in that directory will be lost as they are auto-generated templates.

If your locale is present here, copy the file, and translate all entries that read "UNLOCALIZED". 
	If you do not want to translate all of it, simply delete all entries that are still UNLOCALIZED, this will make it equivalent to an "outdated" lang file.
	Once you are finished, continue with step 3. 



2. Creating a new localization file 

If you couldn't find a started translation in the previous step:
Grab a copy of the full standard localization file here:
https://github.com/Creators-of-Create/Create/blob/mc1.16/dev/src/generated/resources/assets/create/lang/en_us.json
	Rename your copied file to the locale it is targeting, and start making your translations by replacing the english text.
	Once you are finished, continue with step 3. 



3. Publishing your changes

Any lang file you created or changed belongs into this folder:
https://github.com/Creators-of-Create/Create/blob/mc1.16/dev/src/main/resources/assets/create/lang
This is the lang folder you found this readme in.
	You can either open a pull request inserting your file for us, or get in contact on the feedback & help channel linked on Create's project page (CurseForge).
	NOTICE: when making PRs, always target the main branch unless your changes are exclusive to a different version of minecraft than the one the main branch is on.



Thank you kindly for your contribution!
