extern "C" {

#include <Windows.h>
#include <stdio.h>
#include <conio.h>
#include <leechcore.h>
#include <vmmdll.h>

#ifdef EXPORTED_FUNCTION
#undef EXPORTED_FUNCTION
#endif

#define EXPORTED_FUNCTION __declspec(dllexport)

	VMM_HANDLE hVMM = NULL;

	PVMMDLL_MAP_VAD pVadMap = NULL;

	EXPORTED_FUNCTION _Success_(return)
	BOOL Initialize(_In_ DWORD argc, _In_ LPSTR argv[])
	{
		hVMM = VMMDLL_Initialize(argc, argv);
		return hVMM != NULL;
	}

	EXPORTED_FUNCTION _Success_(return)
	BOOL MemReadEx(_In_ DWORD dwPID, _In_ ULONG64 qwA, _Out_writes_(cb) PBYTE pb, _In_ DWORD cb, _Out_opt_ PDWORD pcbReadOpt, _In_ ULONG64 flags)
	{
		return VMMDLL_MemReadEx(hVMM, dwPID, qwA, pb, cb, pcbReadOpt, flags);
	}

	EXPORTED_FUNCTION _Success_(return)
	BOOL MemWrite(_In_ DWORD dwPID, _In_ ULONG64 qwA, _In_reads_(cb) PBYTE pb, _In_ DWORD cb)
	{
		return VMMDLL_MemWrite(hVMM, dwPID, qwA, pb, cb);
	}

	EXPORTED_FUNCTION _Success_(return)
	BOOL PidList(_Out_writes_opt_(*pcPIDs) PDWORD pPIDs, _Inout_ PSIZE_T pcPIDs)
	{
		return VMMDLL_PidList(hVMM, pPIDs, pcPIDs);
	}

	///////////////////////////////////
	EXPORTED_FUNCTION _Success_(return)
	BOOL Map_GetVadW_init(_In_ DWORD dwPID, _In_ BOOL fIdentifyModules)
	{
		if (pVadMap != NULL)
		{
			VMMDLL_MemFree(pVadMap);
		}
		return VMMDLL_Map_GetVadW(hVMM, dwPID, fIdentifyModules, &pVadMap); 
	}

	/*
	* Get VadMap element count
	*/
	EXPORTED_FUNCTION _Success_(return)
	ULONG64 Map_GetVadW_Count()
	{
		if (pVadMap != NULL)
		{
			return pVadMap->cMap;
		}
		return 0;
	}

	/*
	* Get VadMap struct array
	*/
	EXPORTED_FUNCTION _Success_(return)
	BOOL Map_GetVadW_Get(_Out_ VOID * pMap, ULONG64 start, ULONG64 count)
	{
		ULONG64 size = count * sizeof(VMMDLL_MAP_VADENTRY);
		memcpy(pMap, &pVadMap->pMap[start], size);
		return TRUE;
	}

	EXPORTED_FUNCTION _Success_(return)
	ULONG64 GetVADEntrySize()
	{
		return sizeof(VMMDLL_MAP_VADENTRY);
	}
	///////////////////////////////////

	EXPORTED_FUNCTION
	LPSTR ProcessGetInformationString(_In_ DWORD dwPID, _In_ DWORD fOptionString)
	{
		return VMMDLL_ProcessGetInformationString(hVMM, dwPID, fOptionString);
	}

	EXPORTED_FUNCTION
	ULONG64 ProcessGetModuleBaseW(_In_ DWORD dwPID, _In_ LPWSTR wszModuleName)
	{
		return VMMDLL_ProcessGetModuleBaseW(hVMM, dwPID, wszModuleName);
	}

} //extern "C"